package eureca.capstone.project.orchestrator.transaction_feed.service.impl;

import eureca.capstone.project.orchestrator.common.exception.code.ErrorCode;
import eureca.capstone.project.orchestrator.common.exception.custom.InternalServerException;
import eureca.capstone.project.orchestrator.common.exception.custom.TransactionFeedNotFoundException;
import eureca.capstone.project.orchestrator.common.exception.custom.UserNotFoundException;
import eureca.capstone.project.orchestrator.common.util.SalesTypeManager;
import eureca.capstone.project.orchestrator.transaction_feed.dto.enums.WishListFilter;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.AddWishFeedRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.RemoveWishFeedsRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.GetFeedSummaryResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.entity.Liked;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.transaction_feed.repository.LikedRepository;
import eureca.capstone.project.orchestrator.transaction_feed.repository.TransactionFeedRepository;
import eureca.capstone.project.orchestrator.transaction_feed.service.LikedService;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikedServiceImpl implements LikedService {
    private final LikedRepository likedRepository;
    private final UserRepository userRepository;
    private final TransactionFeedRepository transactionFeedRepository;
    private final SalesTypeManager salesTypeManager;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(readOnly = true)
    public Page<GetFeedSummaryResponseDto> getWishList(String email, WishListFilter filter, Pageable pageable) {
        log.info("[getWishList] 사용자 {}의 찜 목록 조회 시작. 필터: {}", email, filter);
        User user = findUserByEmail(email);

        List<Long> likedFeedIds = likedRepository.findFeedIdsByUser(user);
        if (CollectionUtils.isEmpty(likedFeedIds)) {
            log.info("[getWishList] 찜한 판매글이 없습니다.");
            return Page.empty(pageable);
        }
        log.info("[getWishList] 찜한 판매글 {}개 발견.", likedFeedIds.size());

        Page<TransactionFeed> feedPage = transactionFeedRepository.findWishedFeeds(likedFeedIds, filter, pageable);
        log.info("[getWishList] 필터링 및 페이징 조회 완료. 조회된 개수: {}", feedPage.getNumberOfElements());

        Map<Long, Long> highestPriceMap = getHighestPricesFromRedisForFeeds(feedPage.getContent());

        List<GetFeedSummaryResponseDto> dtoList = feedPage.getContent().stream()
                .map(feed -> GetFeedSummaryResponseDto.fromEntity(feed, true, highestPriceMap))
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, feedPage.getTotalElements());
    }

    @Override
    @Transactional
    public void addWishFeed(String email, AddWishFeedRequestDto requestDto) {
        User user = findUserByEmail(email);
        TransactionFeed transactionFeed = findTransactionFeedById(requestDto.getTransactionFeedId());
        log.info("[addWishFeed] 사용자 및 판매글 조회 완료.");

        if (transactionFeed.isDeleted()) throw new TransactionFeedNotFoundException();
        if (likedRepository.existsByFeedAndUser(transactionFeed, user)) throw new InternalServerException(ErrorCode.ALREADY_EXISTS_LIKED_LIST);
        log.info("[addWishFeed] 찜 목록에 존재 X.");

        Liked liked = Liked.builder()
                .user(user)
                .transactionFeed(transactionFeed)
                .build();
        likedRepository.save(liked);
        log.info("[addWishFeed] 찜 목록에 추가 완료. 사용자: {}, 판매글: {}", user.getUserId(), transactionFeed.getTransactionFeedId());
    }

    @Override
    @Transactional
    public void removeWishFeed(String email, RemoveWishFeedsRequestDto requestDto) {
        List<Long> transactionFeedIds = requestDto.getTransactionFeedIds();

        if (CollectionUtils.isEmpty(transactionFeedIds)) {
            log.info("[removeWishFeed] 삭제할 ID 리스트가 비어있어 작업을 종료합니다.");
            return;
        }

        User user = findUserByEmail(email);
        log.info("[removeWishFeed] 사용자 {}의 찜 목록 삭제 시작. 대상 판매글 수: {}", user.getUserId(), transactionFeedIds.size());

        likedRepository.removeByUserAndFeedIds(transactionFeedIds, user);
        log.info("[removeWishFeed] 찜 목록에서 삭제 완료. 사용자: {}, 대상 판매글: {}", user.getUserId(), transactionFeedIds);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }

    private TransactionFeed findTransactionFeedById(Long transactionFeedId) {
        return transactionFeedRepository.findById(transactionFeedId)
                .orElseThrow(TransactionFeedNotFoundException::new);
    }

    private Map<Long, Long> getHighestPricesFromRedisForFeeds(List<TransactionFeed> feeds) {
        log.info("[getHighestPrices] Redis 에서 판매글 최고가 조회 시작. 판매글 수: {}", feeds.size());
        Long bidSalesTypeId = salesTypeManager.getBidSaleType().getSalesTypeId();
        List<Long> auctionFeedIds = new ArrayList<>();
        List<String> redisKeys = new ArrayList<>();

        feeds.stream()
                .filter(feed -> bidSalesTypeId.equals(feed.getSalesType().getSalesTypeId()))
                .forEach(feed -> {
                    auctionFeedIds.add(feed.getTransactionFeedId());
                    redisKeys.add(String.format("bids:%d:highest_price", feed.getTransactionFeedId()));
                });

        if (redisKeys.isEmpty()) {
            return Collections.emptyMap();
        }

        log.info("[getHighestPrices] {}개의 입찰 판매글 최고가를 Redis 에서 조회", redisKeys.size());
        List<String> highestPrices = stringRedisTemplate.opsForValue().multiGet(redisKeys);

        return IntStream.range(0, auctionFeedIds.size())
                .filter(i -> highestPrices != null && highestPrices.get(i) != null)
                .boxed()
                .collect(Collectors.toMap(
                        auctionFeedIds::get,
                        i -> Long.parseLong(highestPrices.get(i))
                ));
    }
}
