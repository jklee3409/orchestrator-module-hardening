package eureca.capstone.project.orchestrator.transaction_feed.service.impl;

import eureca.capstone.project.orchestrator.alarm.service.impl.NotificationProducer;
import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.common.exception.custom.BidException;
import eureca.capstone.project.orchestrator.common.util.SalesTypeManager;
import eureca.capstone.project.orchestrator.common.util.StatusManager;
import eureca.capstone.project.orchestrator.pay.entity.UserPay;
import eureca.capstone.project.orchestrator.pay.repository.UserPayRepository;
import eureca.capstone.project.orchestrator.pay.service.UserPayService;
import eureca.capstone.project.orchestrator.transaction_feed.document.TransactionFeedDocument;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.PlaceBidRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.entity.Bids;
import eureca.capstone.project.orchestrator.transaction_feed.entity.SalesType;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.transaction_feed.repository.BidsRepository;
import eureca.capstone.project.orchestrator.transaction_feed.repository.TransactionFeedRepository;
import eureca.capstone.project.orchestrator.transaction_feed.repository.TransactionFeedSearchRepository;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
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
    private TransactionFeedSearchRepository transactionFeedSearchRepository;
    private BidsRepository bidsRepository;
    private UserPayService userPayService;
    private UserPayRepository userPayRepository;
    private StringRedisTemplate stringRedisTemplate;
    private RedisScript<List> bidScript;
    private StatusManager statusManager;
    private SalesTypeManager salesTypeManager;
    private NotificationProducer notificationProducer;
    private ValueOperations<String, String> valueOperations;

    private BidServiceImpl bidService;

    private User bidder;
    private TransactionFeed feed;
    private PlaceBidRequestDto request;
    private Status onSaleStatus;
    private SalesType bidSalesType;
    private TransactionFeedDocument document;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        transactionFeedRepository = mock(TransactionFeedRepository.class);
        transactionFeedSearchRepository = mock(TransactionFeedSearchRepository.class);
        bidsRepository = mock(BidsRepository.class);
        userPayService = mock(UserPayService.class);
        userPayRepository = mock(UserPayRepository.class);
        stringRedisTemplate = mock(StringRedisTemplate.class);
        bidScript = mock(RedisScript.class);
        statusManager = mock(StatusManager.class);
        salesTypeManager = mock(SalesTypeManager.class);
        notificationProducer = mock(NotificationProducer.class);
        valueOperations = mock(ValueOperations.class);

        bidService = new BidServiceImpl(
                userRepository,
                transactionFeedRepository,
                transactionFeedSearchRepository,
                bidsRepository,
                userPayService,
                userPayRepository,
                stringRedisTemplate,
                bidScript,
                statusManager,
                salesTypeManager,
                notificationProducer
        );

        bidder = mock(User.class, RETURNS_DEEP_STUBS);
        feed = mock(TransactionFeed.class, RETURNS_DEEP_STUBS);
        request = mock(PlaceBidRequestDto.class);
        onSaleStatus = mock(Status.class);
        bidSalesType = mock(SalesType.class);
        document = mock(TransactionFeedDocument.class);

        givenAuctionPrecondition();
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("레디스 멀티키 Lua 호출은 같은 hash tag 키를 사용해야 한다")
    void 레디스_멀티키_Lua_호출은_같은_hash_tag_키를_사용해야_한다() {
        doReturn((List) List.of("BID_TOO_LOW", "0", "0"))
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

        assertThat(keysCaptor.getValue())
                .containsExactly(
                        "bids:{100}:highest_price",
                        "bids:{100}:highest_bidder_id"
                );
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("첫 입찰 후속 처리 실패 시 Redis 최고가를 0으로 복원하면 안 된다")
    void 첫_입찰_후속_처리_실패시_Redis_최고가를_0으로_복원하면_안_된다() {
        doReturn((List) List.of("SUCCESS", "0", "0"))
                .when(stringRedisTemplate)
                .execute(eq(bidScript), anyList(), any(), any(), any());

        doThrow(new RuntimeException("pay use failed"))
                .when(userPayService)
                .usePay(bidder, BID_AMOUNT);

        assertThatThrownBy(() -> bidService.placeBid(EMAIL, request))
                .isInstanceOf(RuntimeException.class);

        verify(valueOperations, never()).set(anyString(), eq("0"));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("입찰 확정 도중 예외가 나면 ES 반영과 알림 발송은 커밋 이후로 미뤄야 한다")
    void 입찰_확정_도중_예외가_나면_ES_반영과_알림_발송은_커밋_이후로_미뤄야_한다() {
        doReturn((List) List.of("SUCCESS", "0", "0"))
                .when(stringRedisTemplate)
                .execute(eq(bidScript), anyList(), any(), any(), any());

        when(transactionFeedSearchRepository.findById(FEED_ID)).thenReturn(Optional.of(document));
        when(transactionFeedSearchRepository.save(any(TransactionFeedDocument.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Bids existingBid = mock(Bids.class);
        when(existingBid.getUser()).thenReturn(bidder);
        when(bidsRepository.findBidsWithUserByTransactionFeed(feed)).thenReturn(List.of(existingBid));

        doThrow(new RuntimeException("notification failed"))
                .when(notificationProducer)
                .send(any());

        assertThatThrownBy(() -> bidService.placeBid(EMAIL, request))
                .isInstanceOf(RuntimeException.class);

        verify(transactionFeedSearchRepository, never()).save(any(TransactionFeedDocument.class));
        verify(notificationProducer, never()).send(any());
    }

    private void givenAuctionPrecondition() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(bidder));
        when(transactionFeedRepository.findById(FEED_ID)).thenReturn(Optional.of(feed));
        when(statusManager.getStatus("FEED", "ON_SALE")).thenReturn(onSaleStatus);
        when(salesTypeManager.getBidSaleType()).thenReturn(bidSalesType);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        when(request.getTransactionFeedId()).thenReturn(FEED_ID);
        when(request.getBidAmount()).thenReturn(BID_AMOUNT);

        when(feed.getTransactionFeedId()).thenReturn(FEED_ID);
        when(feed.getSalesPrice()).thenReturn(SALES_PRICE);
        when(feed.getTitle()).thenReturn("무선 데이터 거래 글");
        when(feed.getStatus()).thenReturn(onSaleStatus);
        when(feed.getSalesType()).thenReturn(bidSalesType);
        when(feed.getExpiresAt()).thenReturn(LocalDateTime.now().plusHours(1));
        when(feed.getUser().getUserId()).thenReturn(SELLER_ID);
        when(feed.getTelecomCompany().getTelecomCompanyId()).thenReturn(10L);

        when(bidder.getUserId()).thenReturn(BIDDER_ID);
        when(bidder.getNickname()).thenReturn("새입찰자");
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