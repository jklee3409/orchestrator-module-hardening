package eureca.capstone.project.orchestrator.transaction_feed.service.impl;

import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.common.exception.custom.BidException;
import eureca.capstone.project.orchestrator.common.util.SalesTypeManager;
import eureca.capstone.project.orchestrator.common.util.StatusManager;
import eureca.capstone.project.orchestrator.pay.entity.UserPay;
import eureca.capstone.project.orchestrator.pay.repository.UserPayRepository;
import eureca.capstone.project.orchestrator.pay.service.UserPayService;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.PlaceBidRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.entity.Bids;
import eureca.capstone.project.orchestrator.transaction_feed.entity.SalesType;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.transaction_feed.event.BidSucceededEvent;
import eureca.capstone.project.orchestrator.transaction_feed.repository.BidsRepository;
import eureca.capstone.project.orchestrator.transaction_feed.repository.TransactionFeedRepository;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BidServiceImplRegressionTest {

    private static final Long FEED_ID = 100L;
    private static final Long BIDDER_ID = 1L;
    private static final Long SELLER_ID = 2L;
    private static final Long BID_AMOUNT = 12_000L;
    private static final Long SALES_PRICE = 10_000L;
    private static final String EMAIL = "bidder@test.com";

    private UserRepository userRepository;
    private TransactionFeedRepository transactionFeedRepository;
    private BidsRepository bidsRepository;
    private UserPayService userPayService;
    private UserPayRepository userPayRepository;
    private StringRedisTemplate stringRedisTemplate;
    private RedisScript<List> bidScript;
    private RedisScript<Long> bidRollbackScript;
    private StatusManager statusManager;
    private SalesTypeManager salesTypeManager;
    private ApplicationEventPublisher applicationEventPublisher;

    private BidServiceImpl bidService;

    private User bidder;
    private TransactionFeed feed;
    private PlaceBidRequestDto request;
    private Status onSaleStatus;
    private SalesType bidSalesType;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        transactionFeedRepository = mock(TransactionFeedRepository.class);
        bidsRepository = mock(BidsRepository.class);
        userPayService = mock(UserPayService.class);
        userPayRepository = mock(UserPayRepository.class);
        stringRedisTemplate = mock(StringRedisTemplate.class);
        bidScript = mock(RedisScript.class);
        bidRollbackScript = mock(RedisScript.class);
        statusManager = mock(StatusManager.class);
        salesTypeManager = mock(SalesTypeManager.class);
        applicationEventPublisher = mock(ApplicationEventPublisher.class);

        bidService = new BidServiceImpl(
                userRepository,
                transactionFeedRepository,
                bidsRepository,
                userPayService,
                userPayRepository,
                stringRedisTemplate,
                bidScript,
                bidRollbackScript,
                statusManager,
                salesTypeManager,
                applicationEventPublisher
        );

        bidder = mock(User.class, RETURNS_DEEP_STUBS);
        feed = mock(TransactionFeed.class, RETURNS_DEEP_STUBS);
        request = mock(PlaceBidRequestDto.class);
        onSaleStatus = mock(Status.class);
        bidSalesType = mock(SalesType.class);

        givenAuctionPrecondition();
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("레디스 멀티키 Lua 호출은 같은 hash tag 키와 버전 키를 사용해야 한다")
    void 레디스_멀티키_Lua_호출은_같은_hash_tag_키와_버전_키를_사용해야_한다() {
        doReturn((List) List.of("BID_TOO_LOW", "__nil__", "__nil__", "-1", "0"))
                .when(stringRedisTemplate)
                .execute(eq(bidScript), anyList(), any(), any(), any());

        ArgumentCaptor<List<String>> keysCaptor = ArgumentCaptor.forClass((Class) List.class);

        assertThatThrownBy(() -> bidService.placeBid(EMAIL, request))
                .isInstanceOf(BidException.class);

        verify(stringRedisTemplate).execute(
                eq(bidScript),
                keysCaptor.capture(),
                eq(BID_AMOUNT.toString()),
                eq(BIDDER_ID.toString()),
                eq(SALES_PRICE.toString())
        );

        assertThat(keysCaptor.getValue()).containsExactly(
                "bids:{100}:highest_price",
                "bids:{100}:highest_bidder_id",
                "bids:{100}:state_version"
        );
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("첫 입찰 후속 처리 실패 시 직접 0으로 복원하지 않고 버전 기반 롤백 Lua를 호출해야 한다")
    void 첫_입찰_후속_처리_실패시_직접_0으로_복원하지_않고_버전_기반_롤백_Lua를_호출해야_한다() {
        doReturn((List) List.of("SUCCESS", "__nil__", "__nil__", "1", "0"))
                .when(stringRedisTemplate)
                .execute(eq(bidScript), anyList(), any(), any(), any());

        doReturn(1L).when(stringRedisTemplate)
                .execute(eq(bidRollbackScript), anyList(), any(), any(), any(), any());

        doThrow(new RuntimeException("pay use failed"))
                .when(userPayService)
                .usePay(bidder, BID_AMOUNT);

        assertThatThrownBy(() -> bidService.placeBid(EMAIL, request))
                .isInstanceOf(RuntimeException.class);

        verify(stringRedisTemplate).execute(
                eq(bidRollbackScript),
                eq(List.of(
                        "bids:{100}:highest_price",
                        "bids:{100}:highest_bidder_id",
                        "bids:{100}:state_version"
                )),
                eq("1"),
                eq("__nil__"),
                eq("__nil__"),
                eq("0")
        );
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("입찰 확정 시 환불 대상은 Redis 반환값이 아니라 DB 현재 최고 입찰자여야 한다")
    void 입찰_확정시_환불_대상은_Redis_반환값이_아니라_DB_현재_최고_입찰자여야_한다() {
        doReturn((List) List.of("SUCCESS", "999", "11000", "2", "1"))
                .when(stringRedisTemplate)
                .execute(eq(bidScript), anyList(), any(), any(), any());

        User committedHighestUser = mock(User.class, RETURNS_DEEP_STUBS);
        when(committedHighestUser.getUserId()).thenReturn(77L);

        Bids committedHighestBid = mock(Bids.class);
        when(committedHighestBid.getUser()).thenReturn(committedHighestUser);
        when(committedHighestBid.getBidAmount()).thenReturn(11_500L);

        when(bidsRepository.findTopByTransactionFeedOrderByBidAmountDescBidTimeDesc(feed))
                .thenReturn(Optional.of(committedHighestBid));

        bidService.placeBid(EMAIL, request);

        verify(userPayService).refundPay(committedHighestUser, 11_500L);
        verify(userRepository, never()).findById(999L);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("레디스는 성공했더라도 DB 최고가가 더 높으면 입찰을 실패 처리하고 롤백해야 한다")
    void 레디스는_성공했더라도_DB_최고가가_더_높으면_입찰을_실패_처리하고_롤백해야_한다() {
        doReturn((List) List.of("SUCCESS", "__nil__", "__nil__", "2", "1"))
                .when(stringRedisTemplate)
                .execute(eq(bidScript), anyList(), any(), any(), any());

        doReturn(1L).when(stringRedisTemplate)
                .execute(eq(bidRollbackScript), anyList(), any(), any(), any(), any());

        User committedHighestUser = mock(User.class, RETURNS_DEEP_STUBS);
        when(committedHighestUser.getUserId()).thenReturn(77L);

        Bids committedHighestBid = mock(Bids.class);
        when(committedHighestBid.getUser()).thenReturn(committedHighestUser);
        when(committedHighestBid.getBidAmount()).thenReturn(15_000L);

        when(bidsRepository.findTopByTransactionFeedOrderByBidAmountDescBidTimeDesc(feed))
                .thenReturn(Optional.of(committedHighestBid));

        assertThatThrownBy(() -> bidService.placeBid(EMAIL, request))
                .isInstanceOf(BidException.class);

        verify(userPayService, never()).refundPay(any(), anyLong());
        verify(userPayService, never()).usePay(any(), anyLong());

        verify(stringRedisTemplate).execute(
                eq(bidRollbackScript),
                eq(List.of(
                        "bids:{100}:highest_price",
                        "bids:{100}:highest_bidder_id",
                        "bids:{100}:state_version"
                )),
                eq("2"),
                eq("__nil__"),
                eq("__nil__"),
                eq("1")
        );
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("입찰 확정 단계는 판매글 행 잠금을 통해 직렬화해야 한다")
    void 입찰_확정_단계는_판매글_행_잠금을_통해_직렬화해야_한다() {
        doReturn((List) List.of("SUCCESS", "__nil__", "__nil__", "1", "0"))
                .when(stringRedisTemplate)
                .execute(eq(bidScript), anyList(), any(), any(), any());
        bidService.placeBid(EMAIL, request);

        verify(transactionFeedRepository).findByIdForUpdate(FEED_ID);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("입찰이 최종 커밋되면 ES 반영과 알림 발송은 AFTER_COMMIT 이벤트로 넘겨야 한다")
    void 입찰이_최종_커밋되면_ES_반영과_알림_발송은_AFTER_COMMIT_이벤트로_넘겨야_한다() {
        doReturn((List) List.of("SUCCESS", "__nil__", "__nil__", "1", "0"))
                .when(stringRedisTemplate)
                .execute(eq(bidScript), anyList(), any(), any(), any());

        when(bidder.getNickname()).thenReturn("새입찰자");
        when(feed.getTitle()).thenReturn("무선 데이터 거래 글");
        bidService.placeBid(EMAIL, request);

        ArgumentCaptor<BidSucceededEvent> eventCaptor = ArgumentCaptor.forClass(BidSucceededEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());

        BidSucceededEvent event = eventCaptor.getValue();
        assertThat(event.transactionFeedId()).isEqualTo(FEED_ID);
        assertThat(event.bidderUserId()).isEqualTo(BIDDER_ID);
        assertThat(event.bidderNickname()).isEqualTo("새입찰자");
        assertThat(event.feedTitle()).isEqualTo("무선 데이터 거래 글");
        assertThat(event.bidAmount()).isEqualTo(BID_AMOUNT);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("입찰 확정 단계에서 예외가 나면 AFTER_COMMIT 이벤트를 발행하면 안 된다")
    void 입찰_확정_단계에서_예외가_나면_AFTER_COMMIT_이벤트를_발행하면_안_된다() {
        doReturn((List) List.of("SUCCESS", "__nil__", "__nil__", "1", "0"))
                .when(stringRedisTemplate)
                .execute(eq(bidScript), anyList(), any(), any(), any());

        doReturn(1L).when(stringRedisTemplate)
                .execute(eq(bidRollbackScript), anyList(), any(), any(), any(), any());

        doThrow(new RuntimeException("save bid history failed"))
                .when(bidsRepository)
                .save(any(Bids.class));

        assertThatThrownBy(() -> bidService.placeBid(EMAIL, request))
                .isInstanceOf(RuntimeException.class);

        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    private void givenAuctionPrecondition() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(bidder));
        when(transactionFeedRepository.findById(FEED_ID)).thenReturn(Optional.of(feed));
        lenient().when(transactionFeedRepository.findByIdForUpdate(FEED_ID)).thenReturn(Optional.of(feed));
        when(statusManager.getStatus("FEED", "ON_SALE")).thenReturn(onSaleStatus);
        when(salesTypeManager.getBidSaleType()).thenReturn(bidSalesType);
        when(request.getTransactionFeedId()).thenReturn(FEED_ID);
        when(request.getBidAmount()).thenReturn(BID_AMOUNT);

        when(feed.getTransactionFeedId()).thenReturn(FEED_ID);
        when(feed.getSalesPrice()).thenReturn(SALES_PRICE);
        when(feed.getStatus()).thenReturn(onSaleStatus);
        when(feed.getSalesType()).thenReturn(bidSalesType);
        when(feed.getExpiresAt()).thenReturn(LocalDateTime.now().plusHours(1));
        when(feed.getUser().getUserId()).thenReturn(SELLER_ID);
        when(feed.getTelecomCompany().getTelecomCompanyId()).thenReturn(10L);

        when(bidder.getUserId()).thenReturn(BIDDER_ID);
        when(bidder.getTelecomCompany().getTelecomCompanyId()).thenReturn(10L);

        when(userPayRepository.findByUserId(BIDDER_ID))
                .thenReturn(Optional.of(
                        UserPay.builder()
                                .userId(BIDDER_ID)
                                .user(bidder)
                                .pay(50_000L)
                                .build()
                ));
    }
}
