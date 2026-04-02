package eureca.capstone.project.orchestrator.transaction_feed.service.impl;

import eureca.capstone.project.orchestrator.alarm.service.impl.NotificationProducer;
import eureca.capstone.project.orchestrator.common.constant.RedisConstant;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BidServiceImplTest {

    private static final Long FEED_ID = 100L;
    private static final Long BIDDER_ID = 1L;
    private static final Long SELLER_ID = 2L;
    private static final Long BID_AMOUNT = 12_000L;
    private static final Long SALES_PRICE = 10_000L;
    private static final Long TELECOM_COMPANY_ID = 10L;
    private static final String EMAIL = "bidder@test.com";
    private static final String SELLER_EMAIL = "seller@test.com";

    private UserRepository userRepository;
    private TransactionFeedRepository transactionFeedRepository;
    private TransactionFeedSearchRepository transactionFeedSearchRepository;
    private BidsRepository bidsRepository;
    private UserPayService userPayService;
    private UserPayRepository userPayRepository;
    private StringRedisTemplate stringRedisTemplate;
    private RedisScript<List> bidScript;
    private RedisScript<Long> bidRollbackScript;
    private StatusManager statusManager;
    private SalesTypeManager salesTypeManager;
    private NotificationProducer notificationProducer;
    private ApplicationEventPublisher applicationEventPublisher;

    private BidServiceImpl bidService;

    private User bidder;
    private User seller;
    private TransactionFeed feed;
    private PlaceBidRequestDto request;
    private Status onSaleStatus;
    private SalesType bidSalesType;

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
        bidRollbackScript = mock(RedisScript.class);
        statusManager = mock(StatusManager.class);
        salesTypeManager = mock(SalesTypeManager.class);
        notificationProducer = mock(NotificationProducer.class);
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

        bidder = mockUser(BIDDER_ID, EMAIL, "bidder");
        seller = mockUser(SELLER_ID, SELLER_EMAIL, "seller");
        feed = mock(TransactionFeed.class, RETURNS_DEEP_STUBS);
        request = mockRequest(BID_AMOUNT);
        onSaleStatus = mock(Status.class);
        bidSalesType = mock(SalesType.class);

        givenFeedPrecondition();
        givenAuctionBidder(bidder, EMAIL, BIDDER_ID, 50_000L);
        givenAuctionBidder(seller, SELLER_EMAIL, SELLER_ID, 50_000L);
        lenient().when(bidsRepository.findBidsWithUserByTransactionFeed(feed)).thenReturn(List.of());
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("첫 입찰 성공 시 입찰 내역이 저장되어야 한다")
    void 첫_입찰_성공_시_입찰_내역이_저장되어야_한다() {
        doReturn(List.of("SUCCESS", "__nil__", "__nil__", "1", "0"))
                .when(stringRedisTemplate)
                .execute(eq(bidScript), anyList(), any(), any(), any());

        bidService.placeBid(EMAIL, request);

        ArgumentCaptor<Bids> bidsCaptor = ArgumentCaptor.forClass(Bids.class);
        verify(bidsRepository).save(bidsCaptor.capture());

        assertThat(bidsCaptor.getValue().getUser()).isEqualTo(bidder);
        assertThat(bidsCaptor.getValue().getBidAmount()).isEqualTo(BID_AMOUNT);
        assertThat(bidsCaptor.getValue().getBidTime()).isNotNull();

        verify(userPayService).usePay(bidder, BID_AMOUNT);
        verify(userPayService, never()).refundPay(any(User.class), anyLong());
        verify(stringRedisTemplate, never()).execute(eq(bidRollbackScript), anyList(), any(), any(), any(), any());
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("입찰 금액이 현재 최고가보다 낮으면 예외가 발생해야 한다")
    void 입찰_금액이_현재_최고가보다_낮으면_예외가_발생해야_한다() {
        PlaceBidRequestDto lowBidRequest = mockRequest(9_000L);

        doReturn(List.of("BID_TOO_LOW", "__nil__", "__nil__", "-1", "0"))
                .when(stringRedisTemplate)
                .execute(eq(bidScript), anyList(), any(), any(), any());

        assertThatThrownBy(() -> bidService.placeBid(EMAIL, lowBidRequest))
                .isInstanceOf(BidException.class)
                .extracting(throwable -> ((BidException) throwable).getErrorCode())
                .isEqualTo(ErrorCode.BID_AMOUNT_TOO_LOW);
    }

    @Test
    @DisplayName("판매자는 자신의 판매글에 입찰할 수 없다")
    void 판매자는_자신의_판매글에_입찰할_수_없다() {
        when(userRepository.findByEmail(SELLER_EMAIL)).thenReturn(Optional.of(seller));

        assertThatThrownBy(() -> bidService.placeBid(SELLER_EMAIL, request))
                .isInstanceOf(BidException.class)
                .extracting(throwable -> ((BidException) throwable).getErrorCode())
                .isEqualTo(ErrorCode.SELLER_CANNOT_BID);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("DB 처리 실패 시 버전 기반 Redis 롤백 Lua를 호출해야 한다")
    void DB_처리_실패_시_버전_기반_Redis_롤백_Lua를_호출해야_한다() {
        User previousBidder = mockUser(99L, "previous@test.com", "previous");
        Bids committedHighestBid = mock(Bids.class);

        doReturn(List.of("SUCCESS", "99", "6000", "1", "0"))
                .when(stringRedisTemplate)
                .execute(eq(bidScript), anyList(), any(), any(), any());

        doReturn(1L).when(stringRedisTemplate)
                .execute(eq(bidRollbackScript), anyList(), any(), any(), any(), any());

        when(committedHighestBid.getUser()).thenReturn(previousBidder);
        when(committedHighestBid.getBidAmount()).thenReturn(6000L);
        when(bidsRepository.findTopByTransactionFeedOrderByBidAmountDescBidTimeDesc(feed))
                .thenReturn(Optional.of(committedHighestBid));

        doThrow(new RuntimeException("pay use failed"))
                .when(userPayService)
                .usePay(bidder, BID_AMOUNT);

        assertThatThrownBy(() -> bidService.placeBid(EMAIL, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("pay use failed");

        verify(userPayService).refundPay(previousBidder, 6000L);
        verify(stringRedisTemplate).execute(
                eq(bidRollbackScript),
                eq(redisKeys()),
                eq("1"),
                eq("99"),
                eq("6000"),
                eq("0")
        );
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("A 입찰 DB 실패 롤백이 B 입찰 최고가를 덮어쓰지 않아야 한다")
    void A_입찰_DB_실패_롤백이_B_입찰_최고가를_덮어쓰지_않아야_한다() throws InterruptedException {
        Long bidderAId = 10L;
        Long bidderBId = 11L;
        String bidderAEmail = "bidder-a@test.com";
        String bidderBEmail = "bidder-b@test.com";
        Long bidderABidAmount = 12_000L;
        Long bidderBBidAmount = 13_000L;

        User bidderA = mockUser(bidderAId, bidderAEmail, "bidderA");
        User bidderB = mockUser(bidderBId, bidderBEmail, "bidderB");
        PlaceBidRequestDto bidderARequest = mockRequest(bidderABidAmount);
        PlaceBidRequestDto bidderBRequest = mockRequest(bidderBBidAmount);

        givenAuctionBidder(bidderA, bidderAEmail, bidderAId, 100_000L);
        givenAuctionBidder(bidderB, bidderBEmail, bidderBId, 100_000L);
        Map<String, String> redisState = new ConcurrentHashMap<>();
        AtomicReference<Long> rollbackResult = new AtomicReference<>();
        AtomicReference<Throwable> bidderAThrowable = new AtomicReference<>();
        AtomicReference<Throwable> bidderBThrowable = new AtomicReference<>();
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch bidderAUsePayEntered = new CountDownLatch(1);
        CountDownLatch bidderBFinished = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        doAnswer(invocation -> {
            String currentBidderId = invocation.getArgument(3);

            if (bidderBId.toString().equals(currentBidderId)) {
                assertThat(bidderAUsePayEntered.await(5, TimeUnit.SECONDS)).isTrue();
            }

            return executeBidScript(
                    redisState,
                    invocation.getArgument(2),
                    invocation.getArgument(3),
                    invocation.getArgument(4)
            );
        }).when(stringRedisTemplate).execute(eq(bidScript), anyList(), any(), any(), any());

        doAnswer(invocation -> {
            Long result = executeRollbackScript(
                    redisState,
                    invocation.getArgument(2),
                    invocation.getArgument(3),
                    invocation.getArgument(4),
                    invocation.getArgument(5)
            );
            rollbackResult.set(result);
            return result;
        }).when(stringRedisTemplate).execute(eq(bidRollbackScript), anyList(), any(), any(), any(), any());

        doAnswer(invocation -> {
            User currentBidder = invocation.getArgument(0);
            Long currentBidAmount = invocation.getArgument(1);

            if (currentBidder.getUserId().equals(bidderAId) && currentBidAmount.equals(bidderABidAmount)) {
                bidderAUsePayEntered.countDown();
                assertThat(bidderBFinished.await(5, TimeUnit.SECONDS)).isTrue();
                throw new RuntimeException("bidder A usePay failed");
            }

            return null;
        }).when(userPayService).usePay(any(User.class), anyLong());

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        try {
            executorService.submit(() -> executeConcurrentBid(
                    readyLatch,
                    startLatch,
                    doneLatch,
                    () -> bidService.placeBid(bidderAEmail, bidderARequest),
                    bidderAThrowable
            ));
            executorService.submit(() -> executeConcurrentBid(
                    readyLatch,
                    startLatch,
                    doneLatch,
                    () -> bidService.placeBid(bidderBEmail, bidderBRequest),
                    bidderBThrowable,
                    bidderBFinished
            ));

            assertThat(readyLatch.await(5, TimeUnit.SECONDS)).isTrue();
            startLatch.countDown();
            assertThat(doneLatch.await(5, TimeUnit.SECONDS)).isTrue();
        } finally {
            executorService.shutdown();
            assertThat(executorService.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        }

        assertThat(bidderAThrowable.get())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("bidder A usePay failed");
        assertThat(bidderBThrowable.get()).isNull();

        assertThat(rollbackResult.get()).isEqualTo(0L);
        assertThat(redisState).containsEntry("highestPrice", bidderBBidAmount.toString());
        assertThat(redisState).containsEntry("highestBidder", bidderBId.toString());
        assertThat(redisState).containsEntry("stateVersion", "2");

        ArgumentCaptor<Bids> bidsCaptor = ArgumentCaptor.forClass(Bids.class);
        verify(bidsRepository, times(1)).save(bidsCaptor.capture());
        assertThat(bidsCaptor.getValue().getUser()).isEqualTo(bidderB);
        assertThat(bidsCaptor.getValue().getBidAmount()).isEqualTo(bidderBBidAmount);

        verify(userPayService).usePay(bidderA, bidderABidAmount);
        verify(userPayService).usePay(bidderB, bidderBBidAmount);
        verify(userPayService, never()).refundPay(any(User.class), anyLong());
        verify(stringRedisTemplate).execute(
                eq(bidRollbackScript),
                eq(redisKeys()),
                eq("1"),
                eq("__nil__"),
                eq("__nil__"),
                eq("0")
        );
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("100개의 동시 입찰을 같은 시점에 시작해 최고가 상태가 일관되게 유지되어야 한다")
    void 동시에_100개의_동시_입찰을_같은_시점에_시작해_최고가_상태가_일관되게_유지되어야_한다() throws InterruptedException {
        int numberOfBidders = 100;
        long bidIncrement = 100L;

        List<User> bidders = IntStream.range(0, numberOfBidders)
                .mapToObj(index -> mockUser(
                        100L + index,
                        "bidder" + index + "@test.com",
                        "bidder" + index
                ))
                .toList();

        Map<String, String> redisState = new ConcurrentHashMap<>();
        Map<Long, Long> acceptedBids = new ConcurrentHashMap<>();
        List<Throwable> unexpectedErrors = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch readyLatch = new CountDownLatch(numberOfBidders);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfBidders);

        for (User currentBidder : bidders) {
            givenAuctionBidder(currentBidder, currentBidder.getEmail(), currentBidder.getUserId(), 999_999L);
            lenient().when(userRepository.findById(currentBidder.getUserId())).thenReturn(Optional.of(currentBidder));
        }

        doAnswer(invocation -> {
            List<String> result = executeBidScript(
                    redisState,
                    invocation.getArgument(2),
                    invocation.getArgument(3),
                    invocation.getArgument(4)
            );

            if ("SUCCESS".equals(result.get(0))) {
                acceptedBids.put(
                        Long.parseLong(invocation.getArgument(3)),
                        Long.parseLong(invocation.getArgument(2))
                );
            }

            return result;
        }).when(stringRedisTemplate).execute(eq(bidScript), anyList(), any(), any(), any());

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfBidders);
        try {
            for (int index = 0; index < numberOfBidders; index++) {
                User currentBidder = bidders.get(index);
                long currentBidAmount = SALES_PRICE + ((index + 1L) * bidIncrement);
                PlaceBidRequestDto currentRequest = mockRequest(currentBidAmount);

                executorService.submit(() -> {
                    readyLatch.countDown();

                    try {
                        assertThat(startLatch.await(5, TimeUnit.SECONDS)).isTrue();
                        bidService.placeBid(currentBidder.getEmail(), currentRequest);
                    } catch (BidException exception) {
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.BID_AMOUNT_TOO_LOW);
                    } catch (Throwable throwable) {
                        unexpectedErrors.add(throwable);
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            assertThat(readyLatch.await(5, TimeUnit.SECONDS)).isTrue();
            startLatch.countDown();
            assertThat(doneLatch.await(10, TimeUnit.SECONDS)).isTrue();
        } finally {
            executorService.shutdown();
            assertThat(executorService.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        }

        assertThat(unexpectedErrors).isEmpty();
        assertThat(acceptedBids).isNotEmpty();

        long highestAcceptedBid = acceptedBids.values().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElseThrow();
        Long highestBidderId = acceptedBids.entrySet().stream()
                .filter(entry -> entry.getValue().equals(highestAcceptedBid))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow();

        assertThat(redisState).containsEntry("highestPrice", String.valueOf(highestAcceptedBid));
        assertThat(redisState).containsEntry("highestBidder", String.valueOf(highestBidderId));
        assertThat(redisState).containsEntry("stateVersion", String.valueOf(acceptedBids.size()));

        verify(userPayService, times(acceptedBids.size())).usePay(any(User.class), anyLong());
        verify(userPayService, never()).refundPay(any(User.class), anyLong());
        verify(stringRedisTemplate, never()).execute(eq(bidRollbackScript), anyList(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("입찰 내역 조회 성공 시 입찰 내역 목록을 반환해야 한다")
    void 입찰_내역_조회_성공_시_입찰_내역_목록을_반환해야_한다() {
        Bids existingBid = mock(Bids.class);

        when(existingBid.getUser()).thenReturn(bidder);
        when(existingBid.getBidAmount()).thenReturn(BID_AMOUNT);
        when(existingBid.getBidTime()).thenReturn(LocalDateTime.now());
        when(bidsRepository.findBidsWithUserByTransactionFeed(feed)).thenReturn(List.of(existingBid));

        var result = bidService.getBidHistory(FEED_ID);

        verify(transactionFeedRepository).findById(FEED_ID);
        verify(salesTypeManager).getBidSaleType();
        verify(bidsRepository).findBidsWithUserByTransactionFeed(feed);

        assertThat(result.getBids()).hasSize(1);
        assertThat(result.getBids().get(0).getBidAmount()).isEqualTo(BID_AMOUNT);
        assertThat(result.getBids().get(0).getBidderNickname()).isEqualTo("bidder");
    }

    @Test
    @DisplayName("입찰 판매글이 아닌 경우 예외가 발생해야 한다")
    void 입찰_판매글이_아닌_경우_예외가_발생해야_한다() {
        SalesType directSaleType = mock(SalesType.class);
        when(feed.getSalesType()).thenReturn(directSaleType);

        assertThatThrownBy(() -> bidService.getBidHistory(FEED_ID))
                .isInstanceOf(BidException.class)
                .extracting(throwable -> ((BidException) throwable).getErrorCode())
                .isEqualTo(ErrorCode.FEED_NOT_AUCTION);
    }

    private void givenFeedPrecondition() {
        lenient().when(transactionFeedRepository.findById(FEED_ID)).thenReturn(Optional.of(feed));
        lenient().when(transactionFeedRepository.findByIdForUpdate(FEED_ID)).thenReturn(Optional.of(feed));
        lenient().when(bidsRepository.findTopByTransactionFeedOrderByBidAmountDescBidTimeDesc(feed))
                .thenReturn(Optional.empty());
        lenient().when(statusManager.getStatus("FEED", "ON_SALE")).thenReturn(onSaleStatus);
        lenient().when(salesTypeManager.getBidSaleType()).thenReturn(bidSalesType);

        lenient().when(feed.getTransactionFeedId()).thenReturn(FEED_ID);
        lenient().when(feed.getSalesPrice()).thenReturn(SALES_PRICE);
        lenient().when(feed.getStatus()).thenReturn(onSaleStatus);
        lenient().when(feed.getSalesType()).thenReturn(bidSalesType);
        lenient().when(feed.getExpiresAt()).thenReturn(LocalDateTime.now().plusHours(1));
        lenient().when(feed.getUser().getUserId()).thenReturn(SELLER_ID);
        lenient().when(feed.getTelecomCompany().getTelecomCompanyId()).thenReturn(TELECOM_COMPANY_ID);
        lenient().when(feed.getTitle()).thenReturn("auction feed");
    }

    private void givenAuctionBidder(User currentBidder, String email, Long userId, Long payAmount) {
        lenient().when(userRepository.findByEmail(email)).thenReturn(Optional.of(currentBidder));
        lenient().when(userPayRepository.findByUserId(userId))
                .thenReturn(Optional.of(
                        UserPay.builder()
                                .userId(userId)
                                .user(currentBidder)
                                .pay(payAmount)
                                .build()
                ));
    }

    private User mockUser(Long userId, String email, String nickname) {
        User mockedUser = mock(User.class, RETURNS_DEEP_STUBS);

        lenient().when(mockedUser.getUserId()).thenReturn(userId);
        lenient().when(mockedUser.getEmail()).thenReturn(email);
        lenient().when(mockedUser.getNickname()).thenReturn(nickname);
        lenient().when(mockedUser.getTelecomCompany().getTelecomCompanyId()).thenReturn(TELECOM_COMPANY_ID);

        return mockedUser;
    }

    private PlaceBidRequestDto mockRequest(Long bidAmount) {
        PlaceBidRequestDto mockedRequest = mock(PlaceBidRequestDto.class);

        lenient().when(mockedRequest.getTransactionFeedId()).thenReturn(FEED_ID);
        lenient().when(mockedRequest.getBidAmount()).thenReturn(bidAmount);

        return mockedRequest;
    }

    private List<String> redisKeys() {
        return List.of(
                "bids:{100}:highest_price",
                "bids:{100}:highest_bidder_id",
                "bids:{100}:state_version"
        );
    }

    private void executeConcurrentBid(
            CountDownLatch readyLatch,
            CountDownLatch startLatch,
            CountDownLatch doneLatch,
            ThrowingRunnable runnable,
            AtomicReference<Throwable> throwableReference
    ) {
        executeConcurrentBid(readyLatch, startLatch, doneLatch, runnable, throwableReference, null);
    }

    private void executeConcurrentBid(
            CountDownLatch readyLatch,
            CountDownLatch startLatch,
            CountDownLatch doneLatch,
            ThrowingRunnable runnable,
            AtomicReference<Throwable> throwableReference,
            CountDownLatch afterLatch
    ) {
        readyLatch.countDown();

        try {
            assertThat(startLatch.await(5, TimeUnit.SECONDS)).isTrue();
            runnable.run();
        } catch (Throwable throwable) {
            throwableReference.set(throwable);
        } finally {
            if (afterLatch != null) {
                afterLatch.countDown();
            }
            doneLatch.countDown();
        }
    }

    private List<String> executeBidScript(
            Map<String, String> redisState,
            String bidAmount,
            String bidderId,
            String salesPrice
    ) {
        synchronized (redisState) {
            String previousBidAmount = redisState.get("highestPrice");
            String previousBidderId = redisState.get("highestBidder");
            long previousVersion = Long.parseLong(redisState.getOrDefault("stateVersion", "0"));

            if (bidderId.equals(previousBidderId)) {
                return List.of("SAME_BIDDER", "__nil__", "__nil__", "-1", String.valueOf(previousVersion));
            }

            long currentBidAmount = Long.parseLong(bidAmount);
            long floorPrice = previousBidAmount == null
                    ? Long.parseLong(salesPrice)
                    : Long.parseLong(previousBidAmount);

            if (currentBidAmount <= floorPrice) {
                return List.of("BID_TOO_LOW", "__nil__", "__nil__", "-1", String.valueOf(previousVersion));
            }

            long appliedVersion = previousVersion + 1;
            redisState.put("highestPrice", bidAmount);
            redisState.put("highestBidder", bidderId);
            redisState.put("stateVersion", String.valueOf(appliedVersion));

            return List.of(
                    "SUCCESS",
                    previousBidderId == null ? RedisConstant.REDIS_NULL_SENTINEL : previousBidderId,
                    previousBidAmount == null ? RedisConstant.REDIS_NULL_SENTINEL : previousBidAmount,
                    String.valueOf(appliedVersion),
                    String.valueOf(previousVersion)
            );
        }
    }

    private Long executeRollbackScript(
            Map<String, String> redisState,
            String expectedAppliedVersion,
            String previousBidderId,
            String previousBidAmount,
            String previousVersion
    ) {
        synchronized (redisState) {
            long currentVersion = Long.parseLong(redisState.getOrDefault("stateVersion", "-1"));
            long expectedVersion = Long.parseLong(expectedAppliedVersion);

            if (currentVersion != expectedVersion) {
                return 0L;
            }

            if (RedisConstant.REDIS_NULL_SENTINEL.equals(previousBidderId)
                    || RedisConstant.REDIS_NULL_SENTINEL.equals(previousBidAmount)) {
                redisState.remove("highestPrice");
                redisState.remove("highestBidder");
                redisState.remove("stateVersion");
                return 1L;
            }

            redisState.put("highestPrice", previousBidAmount);
            redisState.put("highestBidder", previousBidderId);

            long rollbackVersion = Long.parseLong(previousVersion);
            if (rollbackVersion <= 0) {
                redisState.remove("stateVersion");
            } else {
                redisState.put("stateVersion", previousVersion);
            }

            return 1L;
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
