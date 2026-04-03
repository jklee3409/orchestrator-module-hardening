package eureca.capstone.project.orchestrator.transaction_feed.service.impl;

import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.common.exception.code.ErrorCode;
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
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BidServiceWithLockTest {

    private static final Long FEED_ID = 100L;
    private static final Long BIDDER_ID = 1L;
    private static final Long SELLER_ID = 2L;
    private static final Long BID_AMOUNT = 12_000L;
    private static final Long SALES_PRICE = 10_000L;
    private static final Long TELECOM_COMPANY_ID = 10L;
    private static final String EMAIL = "bidder@test.com";

    private UserRepository userRepository;
    private TransactionFeedRepository transactionFeedRepository;
    private BidsRepository bidsRepository;
    private UserPayService userPayService;
    private UserPayRepository userPayRepository;
    private StatusManager statusManager;
    private SalesTypeManager salesTypeManager;
    private ApplicationEventPublisher applicationEventPublisher;

    private BidServiceWithLock bidServiceWithLock;

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
        statusManager = mock(StatusManager.class);
        salesTypeManager = mock(SalesTypeManager.class);
        applicationEventPublisher = mock(ApplicationEventPublisher.class);

        bidServiceWithLock = new BidServiceWithLock(
                userRepository,
                transactionFeedRepository,
                bidsRepository,
                userPayService,
                userPayRepository,
                statusManager,
                salesTypeManager,
                applicationEventPublisher
        );

        bidder = mock(User.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        feed = mock(TransactionFeed.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        request = mock(PlaceBidRequestDto.class);
        onSaleStatus = mock(Status.class);
        bidSalesType = mock(SalesType.class);

        givenLockedAuctionPrecondition(50_000L);
    }

    @Test
    @DisplayName("DB 락 방식도 입찰 성공 시 AFTER_COMMIT 이벤트를 발행해야 한다")
    void placeBidWithDbLockPublishesEvent() {
        when(feed.getTitle()).thenReturn("auction feed");
        when(bidder.getNickname()).thenReturn("bidder");
        when(bidsRepository.findTopByTransactionFeedOrderByBidAmountDescBidTimeDesc(feed))
                .thenReturn(Optional.empty());

        bidServiceWithLock.placeBidWithDbLock(EMAIL, request);

        verify(transactionFeedRepository).findByIdWithLock(FEED_ID);
        verify(userPayService).usePay(bidder, BID_AMOUNT);

        ArgumentCaptor<BidSucceededEvent> eventCaptor = ArgumentCaptor.forClass(BidSucceededEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());

        BidSucceededEvent event = eventCaptor.getValue();
        assertThat(event.transactionFeedId()).isEqualTo(FEED_ID);
        assertThat(event.bidderUserId()).isEqualTo(BIDDER_ID);
        assertThat(event.bidderNickname()).isEqualTo("bidder");
        assertThat(event.feedTitle()).isEqualTo("auction feed");
        assertThat(event.bidAmount()).isEqualTo(BID_AMOUNT);
    }

    @Test
    @DisplayName("DB 현재 최고 입찰자를 환불한 뒤 새 입찰자를 차감해야 한다")
    void placeBidWithDbLockRefundsCommittedHighestBid() {
        User committedHighestUser = mock(User.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        Bids committedHighestBid = mock(Bids.class);

        when(committedHighestUser.getUserId()).thenReturn(77L);
        when(committedHighestBid.getUser()).thenReturn(committedHighestUser);
        when(committedHighestBid.getBidAmount()).thenReturn(11_500L);
        when(bidsRepository.findTopByTransactionFeedOrderByBidAmountDescBidTimeDesc(feed))
                .thenReturn(Optional.of(committedHighestBid));

        bidServiceWithLock.placeBidWithDbLock(EMAIL, request);

        InOrder inOrder = org.mockito.Mockito.inOrder(userPayService, bidsRepository, applicationEventPublisher);
        inOrder.verify(userPayService).refundPay(committedHighestUser, 11_500L);
        inOrder.verify(userPayService).usePay(bidder, BID_AMOUNT);
        inOrder.verify(bidsRepository).save(org.mockito.ArgumentMatchers.any(Bids.class));
        inOrder.verify(applicationEventPublisher).publishEvent(org.mockito.ArgumentMatchers.any(BidSucceededEvent.class));
    }

    @Test
    @DisplayName("DB 락 방식도 사용자 잔액 부족을 사전 검증에서 막아야 한다")
    void placeBidWithDbLockRejectsUserPayLack() {
        when(userPayRepository.findByUserId(BIDDER_ID))
                .thenReturn(Optional.of(
                        UserPay.builder()
                                .userId(BIDDER_ID)
                                .user(bidder)
                                .pay(1_000L)
                                .build()
                ));

        assertThatThrownBy(() -> bidServiceWithLock.placeBidWithDbLock(EMAIL, request))
                .isInstanceOf(BidException.class)
                .extracting(throwable -> ((BidException) throwable).getErrorCode())
                .isEqualTo(ErrorCode.USER_PAY_LACK);

        verify(userPayService, never()).usePay(bidder, BID_AMOUNT);
        verify(applicationEventPublisher, never()).publishEvent(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("DB 락 방식도 현재 최고 입찰보다 낮거나 같은 금액을 거절해야 한다")
    void placeBidWithDbLockRejectsLowBidAgainstCommittedHighest() {
        Bids committedHighestBid = mock(Bids.class);
        User committedHighestUser = mock(User.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);

        when(committedHighestUser.getUserId()).thenReturn(77L);
        when(committedHighestBid.getUser()).thenReturn(committedHighestUser);
        when(committedHighestBid.getBidAmount()).thenReturn(15_000L);
        when(bidsRepository.findTopByTransactionFeedOrderByBidAmountDescBidTimeDesc(feed))
                .thenReturn(Optional.of(committedHighestBid));

        assertThatThrownBy(() -> bidServiceWithLock.placeBidWithDbLock(EMAIL, request))
                .isInstanceOf(BidException.class)
                .extracting(throwable -> ((BidException) throwable).getErrorCode())
                .isEqualTo(ErrorCode.BID_AMOUNT_TOO_LOW);

        verify(userPayService, never()).refundPay(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyLong());
        verify(userPayService, never()).usePay(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyLong());
        verify(applicationEventPublisher, never()).publishEvent(org.mockito.ArgumentMatchers.any());
    }

    private void givenLockedAuctionPrecondition(Long payAmount) {
        lenient().when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(bidder));
        lenient().when(transactionFeedRepository.findByIdWithLock(FEED_ID)).thenReturn(Optional.of(feed));
        lenient().when(bidsRepository.findTopByTransactionFeedOrderByBidAmountDescBidTimeDesc(feed))
                .thenReturn(Optional.empty());
        lenient().when(statusManager.getStatus("FEED", "ON_SALE")).thenReturn(onSaleStatus);
        lenient().when(salesTypeManager.getBidSaleType()).thenReturn(bidSalesType);
        lenient().when(request.getTransactionFeedId()).thenReturn(FEED_ID);
        lenient().when(request.getBidAmount()).thenReturn(BID_AMOUNT);

        lenient().when(feed.getTransactionFeedId()).thenReturn(FEED_ID);
        lenient().when(feed.getSalesPrice()).thenReturn(SALES_PRICE);
        lenient().when(feed.getStatus()).thenReturn(onSaleStatus);
        lenient().when(feed.getSalesType()).thenReturn(bidSalesType);
        lenient().when(feed.getExpiresAt()).thenReturn(LocalDateTime.now().plusHours(1));
        lenient().when(feed.getUser().getUserId()).thenReturn(SELLER_ID);
        lenient().when(feed.getTelecomCompany().getTelecomCompanyId()).thenReturn(TELECOM_COMPANY_ID);

        lenient().when(bidder.getUserId()).thenReturn(BIDDER_ID);
        lenient().when(bidder.getEmail()).thenReturn(EMAIL);
        lenient().when(bidder.getTelecomCompany().getTelecomCompanyId()).thenReturn(TELECOM_COMPANY_ID);

        lenient().when(userPayRepository.findByUserId(BIDDER_ID))
                .thenReturn(Optional.of(
                        UserPay.builder()
                                .userId(BIDDER_ID)
                                .user(bidder)
                                .pay(payAmount)
                                .build()
                ));
    }
}
