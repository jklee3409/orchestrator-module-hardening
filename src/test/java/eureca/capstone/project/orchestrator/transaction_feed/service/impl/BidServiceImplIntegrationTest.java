package eureca.capstone.project.orchestrator.transaction_feed.service.impl;

import eureca.capstone.project.orchestrator.alarm.service.impl.NotificationProducer;
import eureca.capstone.project.orchestrator.alarm.service.impl.RedisNotificationSubscriber;
import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.common.entity.TelecomCompany;
import eureca.capstone.project.orchestrator.common.repository.TelecomCompanyRepository;
import eureca.capstone.project.orchestrator.common.util.SalesTypeManager;
import eureca.capstone.project.orchestrator.common.util.StatusManager;
import eureca.capstone.project.orchestrator.pay.entity.UserPay;
import eureca.capstone.project.orchestrator.pay.repository.UserPayRepository;
import eureca.capstone.project.orchestrator.pay.service.UserPayService;
import eureca.capstone.project.orchestrator.support.AbstractContainerIntegrationTest;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.PlaceBidRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.entity.Bids;
import eureca.capstone.project.orchestrator.transaction_feed.entity.SalesType;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.transaction_feed.repository.BidsRepository;
import eureca.capstone.project.orchestrator.transaction_feed.repository.TransactionFeedRepository;
import eureca.capstone.project.orchestrator.transaction_feed.repository.TransactionFeedSearchRepository;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

class BidServiceImplIntegrationTest extends AbstractContainerIntegrationTest {

    private static final long INITIAL_PAY = 100_000L;
    private static final long START_PRICE = 10_000L;
    private static final int BIDDER_COUNT = 100;

    @Autowired private BidServiceImpl bidService;
    @Autowired private UserRepository userRepository;
    @Autowired private UserPayRepository userPayRepository;
    @Autowired private TransactionFeedRepository transactionFeedRepository;
    @Autowired private BidsRepository bidsRepository;
    @Autowired private TelecomCompanyRepository telecomCompanyRepository;
    @Autowired private StringRedisTemplate stringRedisTemplate;
    @Autowired private StatusManager statusManager;
    @Autowired private SalesTypeManager salesTypeManager;
    @Autowired private TransactionTemplate transactionTemplate;

    @MockitoSpyBean private UserPayService userPayService;

    @MockitoBean private NotificationProducer notificationProducer;
    @MockitoBean private TransactionFeedSearchRepository transactionFeedSearchRepository;
    @MockitoBean private RedisNotificationSubscriber redisNotificationSubscriber;

    private Status feedOnSaleStatus;
    private Status userActiveStatus;
    private SalesType bidSalesType;

    @BeforeEach
    void setUp() {
        deleteBidKeys();
        bidsRepository.deleteAllInBatch();
        transactionFeedRepository.deleteAllInBatch();
        userPayRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        telecomCompanyRepository.deleteAllInBatch();

        doNothing().when(notificationProducer).send(any());
        when(transactionFeedSearchRepository.findById(anyLong())).thenReturn(java.util.Optional.empty());

        feedOnSaleStatus = statusManager.getStatus("FEED", "ON_SALE");
        userActiveStatus = statusManager.getStatus("USER", "ACTIVE");
        bidSalesType = salesTypeManager.getBidSaleType();
    }

    @Test
    @DisplayName("100명의 동시 입찰은 실제 Redis와 DB에서 최고가 상태와 잔액 정합성을 유지해야 한다")
    void 백명의_동시_입찰은_실제_Redis와_DB에서_최고가_상태와_잔액_정합성을_유지해야_한다() throws InterruptedException {
        AuctionFixture fixture = createAuctionFixture("concurrent-highest");
        List<User> bidders = createBidders(fixture.telecomCompany(), "concurrent-highest", BIDDER_COUNT, INITIAL_PAY);

        CountDownLatch readyLatch = new CountDownLatch(BIDDER_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(BIDDER_COUNT);
        AtomicInteger successCount = new AtomicInteger();
        List<Throwable> unexpectedErrors = java.util.Collections.synchronizedList(new ArrayList<>());

        ExecutorService executorService = Executors.newFixedThreadPool(BIDDER_COUNT);
        try {
            for (int index = 0; index < BIDDER_COUNT; index++) {
                User bidder = bidders.get(index);
                long bidAmount = START_PRICE + ((index + 1L) * 100L);
                PlaceBidRequestDto request = placeBidRequest(fixture.feed().getTransactionFeedId(), bidAmount);

                executorService.submit(() -> {
                    readyLatch.countDown();

                    try {
                        assertThat(startLatch.await(10, TimeUnit.SECONDS))
                                .as("all bidder threads should wait on the synchronized start gate")
                                .isTrue();
                        bidService.placeBid(bidder.getEmail(), request);
                        successCount.incrementAndGet();
                    } catch (Throwable throwable) {
                        if (throwable instanceof eureca.capstone.project.orchestrator.common.exception.custom.BidException bidException
                                && bidException.getErrorCode().equals(eureca.capstone.project.orchestrator.common.exception.code.ErrorCode.BID_AMOUNT_TOO_LOW)) {
                            return;
                        }
                        unexpectedErrors.add(throwable);
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            assertThat(readyLatch.await(10, TimeUnit.SECONDS))
                    .as("all bidder threads should be ready before the start gate opens")
                    .isTrue();
            startLatch.countDown();
            assertThat(doneLatch.await(30, TimeUnit.SECONDS))
                    .as("all bidder threads should finish within the timeout")
                    .isTrue();
        } finally {
            executorService.shutdown();
            assertThat(executorService.awaitTermination(10, TimeUnit.SECONDS))
                    .as("thread pool should terminate cleanly after concurrent bid test")
                    .isTrue();
        }

        assertThat(unexpectedErrors)
                .as("unexpected exceptions should not occur during concurrent bidding")
                .isEmpty();

        long expectedHighestBid = START_PRICE + (BIDDER_COUNT * 100L);
        User expectedWinner = bidders.get(BIDDER_COUNT - 1);

        TransactionFeed persistedFeed = transactionFeedRepository.findById(fixture.feed().getTransactionFeedId()).orElseThrow();
        List<Bids> bidHistory = bidsRepository.findBidsWithUserByTransactionFeed(persistedFeed);
        long redisStateVersion = Long.parseLong(readRedisValue(stateVersionKey(fixture.feed().getTransactionFeedId())));

        assertThat(readRedisValue(highestPriceKey(fixture.feed().getTransactionFeedId())))
                .as("redis highest price should match the highest successful bid")
                .isEqualTo(String.valueOf(expectedHighestBid));
        assertThat(readRedisValue(highestBidderKey(fixture.feed().getTransactionFeedId())))
                .as("redis highest bidder should match the highest bidder user id")
                .isEqualTo(String.valueOf(expectedWinner.getUserId()));
        assertThat(redisStateVersion)
                .as("redis state version should include every redis-applied bid transition and be at least the committed success count")
                .isGreaterThanOrEqualTo(successCount.get())
                .isLessThanOrEqualTo(BIDDER_COUNT);

        assertThat(bidHistory)
                .as("db should persist one bid history row per successful bid")
                .hasSize(successCount.get());
        assertThat(bidHistory.get(0).getBidAmount())
                .as("latest persisted bid should be the winning bid amount")
                .isEqualTo(expectedHighestBid);
        assertThat(bidHistory.get(0).getUser().getUserId())
                .as("latest persisted bid should belong to the winning bidder")
                .isEqualTo(expectedWinner.getUserId());

        for (User bidder : bidders) {
            long expectedPay = bidder.getUserId().equals(expectedWinner.getUserId())
                    ? INITIAL_PAY - expectedHighestBid
                    : INITIAL_PAY;

            assertThat(userPayRepository.findByUserId(bidder.getUserId()).orElseThrow().getPay())
                    .as("bidder %s should end with the expected committed pay balance", bidder.getEmail())
                    .isEqualTo(expectedPay);
        }
    }

    @Test
    @DisplayName("A 롤백 시도는 B가 선점한 Redis 최고가를 덮어쓰지 않아야 하고 DB 결과도 일관돼야 한다")
    void A_롤백_시도는_B가_선점한_Redis_최고가를_덮어쓰지_않아야_하고_DB_결과도_일관돼야_한다() throws InterruptedException {
        AuctionFixture fixture = createAuctionFixture("rollback-guard");
        User bidderA = createBidder(fixture.telecomCompany(), "rollback-guard-a", INITIAL_PAY);
        User bidderB = createBidder(fixture.telecomCompany(), "rollback-guard-b", INITIAL_PAY);

        long bidderABidAmount = 12_000L;
        long bidderBBidAmount = 13_000L;
        CountDownLatch bidderAUsePayEntered = new CountDownLatch(1);
        CountDownLatch allowBidderAFailure = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);
        AtomicReference<Throwable> bidderAError = new AtomicReference<>();
        AtomicReference<Throwable> bidderBError = new AtomicReference<>();

        doAnswer(invocation -> {
            User currentBidder = invocation.getArgument(0);
            Long bidAmount = invocation.getArgument(1);

            if (currentBidder.getUserId().equals(bidderA.getUserId()) && bidAmount.equals(bidderABidAmount)) {
                invocation.callRealMethod();
                bidderAUsePayEntered.countDown();

                assertThat(allowBidderAFailure.await(10, TimeUnit.SECONDS))
                        .as("A bid should wait until B has already occupied the redis highest bid state")
                        .isTrue();

                throw new RuntimeException("forced failure after real pay deduction");
            }

            return invocation.callRealMethod();
        }).when(userPayService).usePay(any(User.class), anyLong());

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        try {
            executorService.submit(() -> executeBid(
                    bidderA,
                    placeBidRequest(fixture.feed().getTransactionFeedId(), bidderABidAmount),
                    bidderAError,
                    doneLatch
            ));

            assertThat(bidderAUsePayEntered.await(10, TimeUnit.SECONDS))
                    .as("A bid should reach the real pay deduction stage before B starts")
                    .isTrue();

            executorService.submit(() -> executeBid(
                    bidderB,
                    placeBidRequest(fixture.feed().getTransactionFeedId(), bidderBBidAmount),
                    bidderBError,
                    doneLatch
            ));

            awaitRedisBidState(fixture.feed().getTransactionFeedId(), bidderBBidAmount, bidderB.getUserId(), "2");
            allowBidderAFailure.countDown();

            assertThat(doneLatch.await(20, TimeUnit.SECONDS))
                    .as("both A and B bid threads should finish within the timeout")
                    .isTrue();
        } finally {
            executorService.shutdown();
            assertThat(executorService.awaitTermination(10, TimeUnit.SECONDS))
                    .as("thread pool should terminate cleanly after rollback guard test")
                    .isTrue();
        }

        assertThat(bidderAError.get())
                .as("A should fail after the forced DB-side fault injection")
                .isInstanceOf(RuntimeException.class)
                .hasMessage("forced failure after real pay deduction");
        assertThat(bidderBError.get())
                .as("B should still succeed despite A's later rollback attempt")
                .isNull();

        Long feedId = fixture.feed().getTransactionFeedId();
        TransactionFeed persistedFeed = transactionFeedRepository.findById(feedId).orElseThrow();
        List<Bids> bidHistory = bidsRepository.findBidsWithUserByTransactionFeed(persistedFeed);

        assertThat(readRedisValue(highestPriceKey(feedId)))
                .as("redis highest price must remain B's winning bid after A rollback attempt")
                .isEqualTo(String.valueOf(bidderBBidAmount));
        assertThat(readRedisValue(highestBidderKey(feedId)))
                .as("redis highest bidder must remain B after A rollback attempt")
                .isEqualTo(String.valueOf(bidderB.getUserId()));
        assertThat(readRedisValue(stateVersionKey(feedId)))
                .as("redis state version should remain at version 2 after rollback guard rejects A rollback")
                .isEqualTo("2");

        assertThat(bidHistory)
                .as("only B should remain in committed bid history because A's transaction rolled back")
                .hasSize(1);
        assertThat(bidHistory.get(0).getUser().getUserId())
                .as("the only committed bid should belong to B")
                .isEqualTo(bidderB.getUserId());
        assertThat(bidHistory.get(0).getBidAmount())
                .as("the only committed bid amount should be B's bid")
                .isEqualTo(bidderBBidAmount);

        assertThat(userPayRepository.findByUserId(bidderB.getUserId()).orElseThrow().getPay())
                .as("B should pay only the winning bid amount")
                .isEqualTo(INITIAL_PAY - bidderBBidAmount);
        assertThat(userPayRepository.findByUserId(bidderA.getUserId()).orElseThrow().getPay())
                .as("A should end at the original pay balance because A's entire transaction rolled back")
                .isEqualTo(INITIAL_PAY);
    }

    private void executeBid(
            User bidder,
            PlaceBidRequestDto request,
            AtomicReference<Throwable> errorSink,
            CountDownLatch doneLatch
    ) {
        try {
            bidService.placeBid(bidder.getEmail(), request);
        } catch (Throwable throwable) {
            errorSink.set(throwable);
        } finally {
            doneLatch.countDown();
        }
    }

    private AuctionFixture createAuctionFixture(String prefix) {
        TelecomCompany telecomCompany = telecomCompanyRepository.save(
                TelecomCompany.builder()
                        .name(prefix + "-telecom")
                        .build()
        );
        User seller = userRepository.save(
                User.builder()
                        .telecomCompany(telecomCompany)
                        .email(prefix + "-seller-" + UUID.randomUUID() + "@test.com")
                        .password("password")
                        .nickname(prefix + "-seller")
                        .phoneNumber("01000000000")
                        .status(userActiveStatus)
                        .provider("test")
                        .build()
        );

        TransactionFeed feed = transactionFeedRepository.save(
                TransactionFeed.builder()
                        .user(seller)
                        .title(prefix + "-auction")
                        .content("integration bid test")
                        .telecomCompany(telecomCompany)
                        .salesType(bidSalesType)
                        .salesPrice(START_PRICE)
                        .salesDataAmount(1_000L)
                        .defaultImageNumber(1L)
                        .expiresAt(LocalDateTime.now().plusHours(6))
                        .status(feedOnSaleStatus)
                        .isDeleted(false)
                        .build()
        );

        return new AuctionFixture(telecomCompany, seller, feed);
    }

    private List<User> createBidders(TelecomCompany telecomCompany, String prefix, int count, long initialPay) {
        List<User> bidders = new ArrayList<>(count);

        for (int index = 0; index < count; index++) {
            bidders.add(createBidder(telecomCompany, prefix + "-bidder-" + index, initialPay));
        }

        return bidders;
    }

    private User createBidder(TelecomCompany telecomCompany, String prefix, long initialPay) {
        return java.util.Objects.requireNonNull(transactionTemplate.execute(status -> {
            User bidder = userRepository.save(
                    User.builder()
                            .telecomCompany(telecomCompany)
                            .email(prefix + "-" + UUID.randomUUID() + "@test.com")
                            .password("password")
                            .nickname(prefix)
                            .phoneNumber("010" + String.format("%08d", Math.abs(prefix.hashCode()) % 100000000))
                            .status(userActiveStatus)
                            .provider("test")
                            .build()
            );

            UserPay userPay = new UserPay(bidder);
            userPay.charge(initialPay);
            userPayRepository.save(userPay);

            return bidder;
        }));
    }

    private PlaceBidRequestDto placeBidRequest(Long feedId, Long bidAmount) {
        return PlaceBidRequestDto.builder()
                .transactionFeedId(feedId)
                .bidAmount(bidAmount)
                .build();
    }

    private void awaitRedisBidState(Long feedId, Long expectedBidAmount, Long expectedBidderId, String expectedVersion)
            throws InterruptedException {
        long timeoutAt = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);

        while (System.nanoTime() < timeoutAt) {
            if (String.valueOf(expectedBidAmount).equals(readRedisValue(highestPriceKey(feedId)))
                    && String.valueOf(expectedBidderId).equals(readRedisValue(highestBidderKey(feedId)))
                    && expectedVersion.equals(readRedisValue(stateVersionKey(feedId)))) {
                return;
            }

            Thread.sleep(25L);
        }

        throw new AssertionError("timed out waiting for redis bid state to reach expected version " + expectedVersion);
    }

    private void deleteBidKeys() {
        Set<String> bidKeys = stringRedisTemplate.keys("bids:*");
        if (bidKeys != null && !bidKeys.isEmpty()) {
            stringRedisTemplate.delete(bidKeys);
        }
    }

    private String readRedisValue(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    private String highestPriceKey(Long feedId) {
        return "bids:{" + feedId + "}:highest_price";
    }

    private String highestBidderKey(Long feedId) {
        return "bids:{" + feedId + "}:highest_bidder_id";
    }

    private String stateVersionKey(Long feedId) {
        return "bids:{" + feedId + "}:state_version";
    }

    private record AuctionFixture(TelecomCompany telecomCompany, User seller, TransactionFeed feed) {
    }
}
