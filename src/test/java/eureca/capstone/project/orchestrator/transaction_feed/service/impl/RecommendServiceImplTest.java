package eureca.capstone.project.orchestrator.transaction_feed.service.impl;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.common.entity.TelecomCompany;
import eureca.capstone.project.orchestrator.common.util.SalesTypeManager;
import eureca.capstone.project.orchestrator.common.util.StatusManager;
import eureca.capstone.project.orchestrator.transaction_feed.dto.UserTransactionAverageDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.enums.FeedSort;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.FeedSearchRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.GetFeedSummaryResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.entity.SalesType;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.transaction_feed.repository.DataTransactionHistoryRepository;
import eureca.capstone.project.orchestrator.transaction_feed.repository.TransactionFeedRepository;
import eureca.capstone.project.orchestrator.transaction_feed.service.TransactionFeedService;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendServiceImplTest {

    @InjectMocks
    private RecommendServiceImpl recommendService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DataTransactionHistoryRepository dataTransactionHistoryRepository;

    @Mock
    private TransactionFeedRepository transactionFeedRepository;

    @Mock
    private TransactionFeedService transactionFeedService;

    @Mock
    private StatusManager statusManager;

    @Mock
    private SalesTypeManager salesTypeManager;

    private User user;
    private TelecomCompany telecomCompany;
    private SalesType normalSalesType;
    private Status onSaleStatus;
    private List<GetFeedSummaryResponseDto> mockFeeds;
    private CustomUserDetailsDto userDetailsDto;
    private UserTransactionAverageDto userTransactionAverageDto;
    private TransactionFeed transactionFeed;

    @BeforeEach
    void setUp() {
        telecomCompany = TelecomCompany.builder()
                .telecomCompanyId(1L)
                .name("테스트통신사")
                .build();

        user = User.builder()
                .userId(1L)
                .email("test@example.com")
                .nickname("테스트유저")
                .telecomCompany(telecomCompany)
                .build();

        normalSalesType = SalesType.builder()
                .SalesTypeId(1L)
                .name("일반판매")
                .build();

        onSaleStatus = Status.builder()
                .statusId(1L)
                .domain("FEED")
                .code("ON_SALE")
                .build();

        userDetailsDto = CustomUserDetailsDto.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .build();

        userTransactionAverageDto = new UserTransactionAverageDto(5000.0, 500.0);

        transactionFeed = TransactionFeed.builder()
                .transactionFeedId(1L)
                .user(user)
                .title("테스트 판매글")
                .content("테스트 내용입니다")
                .telecomCompany(telecomCompany)
                .salesType(normalSalesType)
                .salesPrice(5000L)
                .salesDataAmount(500L)
                .status(onSaleStatus)
                .build();

        mockFeeds = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            GetFeedSummaryResponseDto feed = GetFeedSummaryResponseDto.builder()
                    .transactionFeedId((long) i)
                    .title("테스트 피드 " + i)
                    .salesPrice(1000L * i)
                    .salesDataAmount(100L * i)
                    .build();
            mockFeeds.add(feed);
        }
    }

    @Test
    @DisplayName("비로그인 사용자에게 가격 기준 피드 추천")
    void recommendFeed_ForGuestUser() {
        // given
        when(transactionFeedService.searchFeeds(any(FeedSearchRequestDto.class), any(Pageable.class), isNull()))
                .thenReturn(new PageImpl<>(mockFeeds));

        // when
        List<GetFeedSummaryResponseDto> result = recommendService.recommendFeed(null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(10);
        verify(transactionFeedService).searchFeeds(argThat(request -> 
                request.getSortBy() == FeedSort.PRICE_LOW), any(Pageable.class), isNull());
    }

    @Test
    @DisplayName("거래 내역이 있는 사용자에게 유사도 기반 피드 추천")
    void recommendFeed_ForUserWithTransactionHistory() {
        // given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(dataTransactionHistoryRepository.findAverageByUser(any(User.class)))
                .thenReturn(Optional.of(userTransactionAverageDto));
        when(statusManager.getStatus(eq("FEED"), eq("ON_SALE"))).thenReturn(onSaleStatus);
        when(salesTypeManager.getNormalSaleType()).thenReturn(normalSalesType);
        when(transactionFeedService.searchFeeds(any(FeedSearchRequestDto.class), any(Pageable.class), isNull()))
                .thenReturn(new PageImpl<>(mockFeeds));

        // when
        List<GetFeedSummaryResponseDto> result = recommendService.recommendFeed(userDetailsDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(10);
        verify(userRepository).findByEmail(user.getEmail());
        verify(dataTransactionHistoryRepository).findAverageByUser(user);
        verify(transactionFeedService).searchFeeds(argThat(request -> 
                request.getSortBy() == FeedSort.LATEST), any(Pageable.class), isNull());
    }
    
    @Test
    @DisplayName("판매글 ID로 관련 피드 추천 성공")
    void recommendRelateFeeds_Success() {
        // Given
        Long feedId = transactionFeed.getTransactionFeedId();
        when(transactionFeedRepository.findById(feedId)).thenReturn(Optional.of(transactionFeed));
        when(statusManager.getStatus(eq("FEED"), eq("ON_SALE"))).thenReturn(onSaleStatus);
        when(transactionFeedService.searchFeeds(any(FeedSearchRequestDto.class), any(Pageable.class), isNull()))
                .thenReturn(new PageImpl<>(mockFeeds));

        // When
        List<GetFeedSummaryResponseDto> result = recommendService.recommendRelateFeeds(feedId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(4); // RELATED_RECOMMENDATION_LIMIT is 4
        verify(transactionFeedRepository).findById(feedId);
        verify(statusManager).getStatus("FEED", "ON_SALE");
        verify(transactionFeedService).searchFeeds(argThat(request -> 
                request.getTelecomCompanyIds().contains(telecomCompany.getTelecomCompanyId()) &&
                request.getSalesTypeIds().contains(normalSalesType.getSalesTypeId()) &&
                request.getExcludeFeedIds().contains(feedId)), 
                any(Pageable.class), isNull());
    }
}