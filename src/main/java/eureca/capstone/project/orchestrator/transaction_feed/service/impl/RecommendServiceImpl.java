package eureca.capstone.project.orchestrator.transaction_feed.service.impl;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.common.entity.TelecomCompany;
import eureca.capstone.project.orchestrator.common.exception.custom.UserNotFoundException;
import eureca.capstone.project.orchestrator.common.util.SalesTypeManager;
import eureca.capstone.project.orchestrator.common.util.StatusManager;
import eureca.capstone.project.orchestrator.transaction_feed.dto.UserTransactionAverageDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.enums.FeedSort;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.FeedSearchRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.GetFeedSummaryResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.entity.SalesType;
import eureca.capstone.project.orchestrator.transaction_feed.repository.DataTransactionHistoryRepository;
import eureca.capstone.project.orchestrator.transaction_feed.service.RecommendService;
import eureca.capstone.project.orchestrator.transaction_feed.service.TransactionFeedService;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendServiceImpl implements RecommendService {
    private final UserRepository userRepository;
    private final DataTransactionHistoryRepository dataTransactionHistoryRepository;
    private final TransactionFeedService transactionFeedService;
    private final StatusManager statusManager;
    private final SalesTypeManager salesTypeManager;

    private static final int RECOMMENDATION_LIMIT = 10;
    private static final int SIMILARITY_CANDIDATE_LIMIT = 1000;

    @Override
    public List<GetFeedSummaryResponseDto> recommendFeed(CustomUserDetailsDto customUserDetailsDto) {
        if (customUserDetailsDto == null) {
            log.info("[recommendFeed] 비로그인 사용자에 대한 추천 시작");
            return recommendForGuest();
        }

        User user = findUserByEmail(customUserDetailsDto.getEmail());
        log.info("[recommendFeed] 사용자 ID: {}에 대한 추천 로직 시작", user.getUserId());

        Optional<UserTransactionAverageDto> averages = dataTransactionHistoryRepository.findAverageByUser(user);
        List<GetFeedSummaryResponseDto> finalRecommendations;

        if (averages.isPresent()) {
            // 거래 내역 있는 사용자: 유사도 기반 추천
            log.info("[recommendFeed] 사용자 ID: {}의 거래 내역 확인. 유사도 기반 추천을 시작.", user.getUserId());
            List<GetFeedSummaryResponseDto> targetFeeds = getTargetTransactionFeeds(user);
            finalRecommendations = recommendBySimilarity(targetFeeds, averages.get());

        } else {
            // 거래 내역 없는 사용자: 가격 기반 추천
            log.info("[recommendFeed] 사용자 ID: {}의 거래 내역이 없어, 가격 기반 추천을 시작.", user.getUserId());
            finalRecommendations = recommendForUserWithoutHistory(user);
        }

        // 추천 개수가 10개 미만일 경우, 전체 피드에서 가격순으로 채우기
        if (finalRecommendations.size() < RECOMMENDATION_LIMIT) {
            log.info("[recommendFeed] 추천 개수 부족. 전체 판매글에서 가격순으로 추가 추천");
            finalRecommendations = fillRemainingWithGlobalCheapest(finalRecommendations);
        }

        log.info("[recommendFeed] 사용자 ID: {}에 대한 최종 추천 개수: {}", user.getUserId(), finalRecommendations.size());
        return finalRecommendations;
    }

    private List<GetFeedSummaryResponseDto> recommendForGuest() {
        FeedSearchRequestDto requestDto = FeedSearchRequestDto.builder()
                .sortBy(FeedSort.PRICE_LOW)
                .build();
        Pageable pageable = PageRequest.of(0, RECOMMENDATION_LIMIT);
        log.info("[recommendForGuest] 비로그인 사용자용 추천(가격 오름차순) 요청");
        return transactionFeedService.searchFeeds(requestDto, pageable, null).getContent();
    }

    private List<GetFeedSummaryResponseDto> recommendForUserWithoutHistory(User user) {
        FeedSearchRequestDto request = createBaseBuilderForUser(user)
                .sortBy(FeedSort.PRICE_LOW).build();
        Pageable pageable = PageRequest.of(0, RECOMMENDATION_LIMIT);
        return transactionFeedService.searchFeeds(request, pageable, null).getContent();
    }

    private List<GetFeedSummaryResponseDto> recommendBySimilarity(List<GetFeedSummaryResponseDto> feeds, UserTransactionAverageDto averages) {
        log.info("[recommendBySimilarity] 유사도 계산 시작. 대상 피드: {}개", feeds.size());
        if (feeds.isEmpty()) {
            return new ArrayList<>();
        }

        LongSummaryStatistics priceStats = feeds.stream()
                .mapToLong(GetFeedSummaryResponseDto::getSalesPrice)
                .summaryStatistics();
        LongSummaryStatistics dataStats = feeds.stream()
                .mapToLong(GetFeedSummaryResponseDto::getSalesDataAmount)
                .summaryStatistics();

        long minPrice = priceStats.getMin();
        long maxPrice = priceStats.getMax();
        long minData = dataStats.getMin();
        long maxData = dataStats.getMax();

        final double priceRange = (maxPrice - minPrice) == 0 ? 1 : (double) (maxPrice - minPrice);
        final double dataRange = (maxData - minData) == 0 ? 1 : (double) (maxData - minData);
        log.info("[recommendBySimilarity] 정규화 범위 계산 완료. PriceRange: {}, DataRange: {}", priceRange, dataRange);

        double userNormPrice = (averages.getAveragePrice() - minPrice) / priceRange;
        double userNormData = (averages.getAverageDataAmount() - minData) / dataRange;
        log.info("[recommendBySimilarity] 사용자 평균 정규화 완료. NormPrice: {}, NormData: {}", userNormPrice, userNormData);

        return feeds.stream()
                .map(feed -> {
                    double feedNormPrice = (feed.getSalesPrice() - minPrice) / priceRange;
                    double feedNormData = (feed.getSalesDataAmount() - minData) / dataRange;
                    double distance = Math.sqrt(Math.pow(userNormPrice - feedNormPrice, 2) + Math.pow(userNormData - feedNormData, 2));
                    return new FeedWithSimilarity(feed, distance);
                })
                .sorted()
                .map(FeedWithSimilarity::feed)
                .limit(RECOMMENDATION_LIMIT)
                .collect(Collectors.toList());
    }

    private List<GetFeedSummaryResponseDto> fillRemainingWithGlobalCheapest(List<GetFeedSummaryResponseDto> currentRecommendations) {
        int needed = RECOMMENDATION_LIMIT - currentRecommendations.size();
        if (needed <= 0) {
            return currentRecommendations;
        }

        List<Long> excludeIds = currentRecommendations.stream()
                .map(GetFeedSummaryResponseDto::getTransactionFeedId)
                .collect(Collectors.toList());

        FeedSearchRequestDto requestDto = FeedSearchRequestDto.builder()
                .sortBy(FeedSort.PRICE_LOW)
                .excludeFeedIds(excludeIds)
                .build();

        Pageable pageable = PageRequest.of(0, needed);
        List<GetFeedSummaryResponseDto> additionalFeeds = transactionFeedService.searchFeeds(requestDto, pageable, null).getContent();

        List<GetFeedSummaryResponseDto> finalRecommendations = new ArrayList<>(currentRecommendations);
        finalRecommendations.addAll(additionalFeeds);

        return finalRecommendations;
    }

    private FeedSearchRequestDto.FeedSearchRequestDtoBuilder createBaseBuilderForUser(User user) {
        TelecomCompany telecomCompany = user.getTelecomCompany();
        Status salesStatus = statusManager.getStatus("FEED", "ON_SALE");
        SalesType normalSalesType = salesTypeManager.getNormalSaleType();

        return FeedSearchRequestDto.builder()
                .telecomCompanyIds(List.of(telecomCompany.getTelecomCompanyId()))
                .salesTypeIds(List.of(normalSalesType.getSalesTypeId()))
                .statuses(List.of(salesStatus.getCode()));
    }

    private List<GetFeedSummaryResponseDto> getTargetTransactionFeeds(User user) {
        log.info("[getTargetTransactionFeeds] 사용자 ID: {}의 추천 타겟 판매글 조회를 시작", user.getUserId());
        FeedSearchRequestDto requestDto = createBaseBuilderForUser(user)
                .sortBy(FeedSort.LATEST)
                .build();

        Pageable pageable = PageRequest.of(0, SIMILARITY_CANDIDATE_LIMIT);

        Page<GetFeedSummaryResponseDto> targetFeeds = transactionFeedService.searchFeeds(requestDto, pageable, null);
        log.info("[getTargetTransactionFeeds] 사용자 ID: {}의 추천 타겟 판매글 {}개 조회", user.getUserId(), targetFeeds.getNumberOfElements());
        return targetFeeds.getContent();
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }

    private record FeedWithSimilarity(GetFeedSummaryResponseDto feed, double similarity) implements Comparable<FeedWithSimilarity> {
        @Override
        public int compareTo(FeedWithSimilarity other) {
            return Double.compare(this.similarity, other.similarity);
        }
    }
}