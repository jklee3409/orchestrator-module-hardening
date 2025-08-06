package eureca.capstone.project.orchestrator.transaction_feed.service.impl;

import eureca.capstone.project.orchestrator.alarm.service.impl.NotificationProducer;
import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.common.entity.TelecomCompany;
import eureca.capstone.project.orchestrator.common.exception.code.ErrorCode;
import eureca.capstone.project.orchestrator.common.exception.custom.BidException;
import eureca.capstone.project.orchestrator.common.exception.custom.InternalServerException;
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
import eureca.capstone.project.orchestrator.transaction_feed.repository.custom.TransactionFeedRepositoryCustom;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BidServiceImplTest {

    @InjectMocks
    private BidServiceImpl bidService;

    @Mock private UserRepository userRepository;

    @Mock private TransactionFeedRepository transactionFeedRepository;

    @Mock private BidsRepository bidsRepository;

    @Mock private StringRedisTemplate stringRedisTemplate;

    @Mock private RedisScript<List> bidScript;

    @Mock private StatusManager statusManager;

    @Mock private SalesTypeManager salesTypeManager;

    @Mock private UserPayService userPayService;

    @Mock private TransactionFeedSearchRepository transactionFeedSearchRepository;

    @Mock private NotificationProducer notificationProducer;

    @Mock private ValueOperations<String, String> valueOperations;

    @Mock private UserPayRepository userPayRepository;

    private User seller;
    private User bidder;
    private TransactionFeed feed;
    private SalesType bidSalesType;
    private Status onSaleStatus;
    private TelecomCompany telecomCompany;
    private String email;
    private Long bidAmount;
    private Long feedId;

    @BeforeEach
    void setUp() {
        telecomCompany = TelecomCompany.builder().telecomCompanyId(1L).name("테스트통신사").build();
        seller = User.builder().userId(1L).email("seller@example.com").nickname("판매자").telecomCompany(telecomCompany).build();
        bidder = User.builder().userId(2L).email("bidder@example.com").nickname("입찰자").telecomCompany(telecomCompany).build();
        bidSalesType = SalesType.builder().SalesTypeId(1L).name("입찰 판매").build();
        onSaleStatus = Status.builder().statusId(1L).domain("FEED").code("ON_SALE").build();

        feed = TransactionFeed.builder()
                .transactionFeedId(1L)
                .user(seller)
                .title("데이터 판매")
                .content("판매합니다")
                .salesPrice(5000L)
                .telecomCompany(telecomCompany)
                .salesType(bidSalesType)
                .expiresAt(LocalDateTime.now().plusDays(10))
                .status(onSaleStatus)
                .build();
    }

    @Nested
    @DisplayName("입찰 기능")
    class PlaceBid {

        @BeforeEach
        void setup() {
            UserPay mockBidderPay = UserPay.builder().pay(100000L).build();
            lenient().when(userPayRepository.findByUserId(bidder.getUserId()))
                    .thenReturn(Optional.of(mockBidderPay));

            UserPay mockSellerPay = UserPay.builder().pay(100000L).build();
            lenient().when(userPayRepository.findByUserId(seller.getUserId()))
                    .thenReturn(Optional.of(mockSellerPay));

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(bidder));
            when(transactionFeedRepository.findById(anyLong())).thenReturn(Optional.of(feed));
            when(statusManager.getStatus("FEED", "ON_SALE")).thenReturn(onSaleStatus);
            when(salesTypeManager.getBidSaleType()).thenReturn(bidSalesType);
//            when(transactionFeedSearchRepository.findById(anyLong())).thenReturn(Optional.of(new TransactionFeedDocument()));
        }

        @Test
        @DisplayName("[성공] 첫 입찰 성공 시 입찰 내역이 저장되어야 한다")
        void placeBid_Success() {
            // given
            PlaceBidRequestDto request = PlaceBidRequestDto.builder()
                    .transactionFeedId(feed.getTransactionFeedId())
                    .bidAmount(6000L)
                    .build();
            List<String> mockResult = Arrays.asList("SUCCESS", "0", "0");

            when(stringRedisTemplate.execute(any(RedisScript.class), any(List.class), anyString(), anyString(), anyString()))
                    .thenReturn(mockResult);

            // when
            bidService.placeBid(bidder.getEmail(), request);

            // then
            ArgumentCaptor<Bids> bidsCaptor = ArgumentCaptor.forClass(Bids.class);
            verify(bidsRepository).save(bidsCaptor.capture());
            Bids savedBids = bidsCaptor.getValue();

            assertThat(savedBids.getBidTime()).isNotNull();

            verify(userPayService).usePay(bidder, 6000L);
            verify(userPayService, never()).refundPay(any(User.class), anyLong()); // 첫 입찰이므로 환불 없음
        }

        @Test
        @DisplayName("[예외] 입찰 금액이 판매가보다 낮으면 예외가 발생한다")
        void placeBid_BidAmountTooLow() {
            // given
            PlaceBidRequestDto request = PlaceBidRequestDto.builder()
                    .transactionFeedId(feed.getTransactionFeedId())
                    .bidAmount(4000L)
                    .build();

            List<String> mockResult = Arrays.asList("BID_TOO_LOW", "0", "0");
            when(stringRedisTemplate.execute(
                    any(RedisScript.class),
                    any(List.class),
                    anyString(),
                    anyString(),
                    anyString()
            )).thenReturn(mockResult);

            // when & then
            BidException exception = assertThrows(BidException.class, () -> bidService.placeBid(bidder.getEmail(), request));
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.BID_AMOUNT_TOO_LOW);
        }

        @Test
        @DisplayName("[예외] 판매자는 자신의 판매글에 입찰할 수 없다")
        void placeBid_SellerCannotBid() {
            // given
            when(userRepository.findByEmail(seller.getEmail())).thenReturn(Optional.of(seller));
            PlaceBidRequestDto request = PlaceBidRequestDto.builder()
                    .transactionFeedId(feed.getTransactionFeedId())
                    .bidAmount(6000L)
                    .build();

            // when & then
            BidException exception = assertThrows(BidException.class, () -> bidService.placeBid(seller.getEmail(), request));
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SELLER_CANNOT_BID);
        }

        @Test
        @DisplayName("[예외] DB 처리 실패 시 Redis 상태를 롤백해야 한다")
        void placeBid_whenDBFails_shouldRollbackRedis() {
            // given
            PlaceBidRequestDto request = PlaceBidRequestDto.builder()
                    .transactionFeedId(feed.getTransactionFeedId())
                    .bidAmount(7000L)
                    .build();
            User prevBidder = User.builder().userId(99L).build();
            Long prevBidAmount = 6000L;
            List<Object> mockResult = List.of("SUCCESS", String.valueOf(prevBidder.getUserId()), String.valueOf(prevBidAmount));

            when(stringRedisTemplate.execute(any(RedisScript.class), any(List.class), anyString(), anyString(), anyString()))
                    .thenReturn(mockResult);
            when(userRepository.findById(prevBidder.getUserId())).thenReturn(Optional.of(prevBidder));

            // 페이 사용 과정에서 DB 예외 발생 가정
            doThrow(new RuntimeException("DB 처리 중 오류 발생!!!")).when(userPayService).usePay(any(), anyLong());

            when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

            // when & then
            assertThrows(RuntimeException.class, () -> bidService.placeBid(bidder.getEmail(), request));

            // Redis 롤백 검증
            String highestPriceKey = String.format("bids:%d:highest_price", feed.getTransactionFeedId());
            String highestBidderKey = String.format("bids:%d:highest_bidder_id", feed.getTransactionFeedId());

            verify(valueOperations).set(highestPriceKey, String.valueOf(prevBidAmount));
            verify(valueOperations).set(highestBidderKey, String.valueOf(prevBidder.getUserId()));
        }

        @Test
        @DisplayName("[안정성] 100개의 유효한 입찰 동시 처리 시, 서비스 로직이 안정적으로 모두 수행되어야 한다")
        void placeBid_concurrency_100_users_success() throws InterruptedException {
            // given
            int numberOfBidders = 100;
            long startPrice = feed.getSalesPrice(); // 5000L
            long bidIncrement = 100L;

            // 입찰자 100명 생성
            List<User> bidders = IntStream.range(0, numberOfBidders)
                    .mapToObj(i -> User.builder().userId(100L + i).email("bidder" + i + "@test.com").nickname("동시입찰자" + i).telecomCompany(telecomCompany).build())
                    .toList();

            // Mock
            for (User b : bidders) {
                when(userRepository.findByEmail(b.getEmail())).thenReturn(Optional.of(b));
                lenient().when(userRepository.findById(b.getUserId())).thenReturn(Optional.of(b));

                UserPay mockPay = UserPay.builder().pay(999999L).build(); // 충분한 금액
                lenient().when(userPayRepository.findByUserId(b.getUserId())).thenReturn(Optional.of(mockPay));
            }

            final Map<String, String> redisState = new ConcurrentHashMap<>();
            redisState.put("highestPrice", String.valueOf(startPrice));
            redisState.put("highestBidder", "0");

            // Redis 스크립트가 이전 입찰자를 반환
            when(stringRedisTemplate.execute(any(RedisScript.class), any(List.class), anyString(), anyString(), anyString()))
                    .thenAnswer(invocation -> {
                        synchronized (redisState) {
                            long currentPrice = Long.parseLong(invocation.getArgument(2));
                            String currentBidderId = invocation.getArgument(3);

                            long prevPrice = Long.parseLong(redisState.get("highestPrice"));
                            String prevBidderId = redisState.get("highestBidder");

                            redisState.put("highestPrice", String.valueOf(currentPrice));
                            redisState.put("highestBidder", currentBidderId);

                            return Arrays.asList("SUCCESS", prevBidderId, String.valueOf(prevPrice));
                        }
                    });

            // when
            ExecutorService executorService = Executors.newFixedThreadPool(numberOfBidders);
            CountDownLatch doneLatch = new CountDownLatch(numberOfBidders);

            for (int i = 0; i < numberOfBidders; i++) {
                User currentBidder = bidders.get(i);
                long currentBidAmount = startPrice + ((i + 1) * bidIncrement);
                PlaceBidRequestDto request = PlaceBidRequestDto.builder().transactionFeedId(feed.getTransactionFeedId()).bidAmount(currentBidAmount).build();

                executorService.submit(() -> {
                    try {
                        bidService.placeBid(currentBidder.getEmail(), request);
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }
            doneLatch.await(); // 모든 스레드가 끝날 때까지 대기
            executorService.shutdown();

            // then
            User winner = bidders.get(numberOfBidders - 1);
            long winningBid = startPrice + (numberOfBidders * bidIncrement);

            // DB 입찰 내역 save 검증
//            verify(bidsRepository, times(numberOfBidders)).save(any(Bids.class));

            // 입찰 성공 시 페이 사용 로직 호출 검증
            for (int i = 0; i < numberOfBidders; i++) {
                User currentBidder = bidders.get(i);
                long currentBidAmount = startPrice + ((i + 1) * bidIncrement);
                verify(userPayService).usePay(eq(currentBidder), eq(currentBidAmount));
            }

            // 이전 입찰자들에 대한 페이 환불 검증
            verify(userPayService, times(numberOfBidders - 1)).refundPay(any(User.class), anyLong());
        }
    }

    @Nested
    @DisplayName("입찰 내역 조회 기능")
    class GetBidHistory {

        @Test
        @DisplayName("입찰 내역 조회 성공 시 입찰 내역 목록이 반환되어야 한다")
        void getBidHistory_Success() {
            // given
            when(transactionFeedRepository.findById(feedId)).thenReturn(Optional.of(feed));
            when(salesTypeManager.getBidSaleType()).thenReturn(bidSalesType);
            
            List<Bids> bidsList = List.of(
                Bids.builder()
                    .bidsId(1L)
                    .transactionFeed(feed)
                    .user(bidder)
                    .bidAmount(10000L)
                    .build()
            );
            
            when(bidsRepository.findBidsWithUserByTransactionFeed(feed)).thenReturn(bidsList);

            // when
            var result = bidService.getBidHistory(feedId);

            // then
            verify(transactionFeedRepository).findById(feedId);
            verify(salesTypeManager).getBidSaleType();
            verify(bidsRepository).findBidsWithUserByTransactionFeed(feed);
            org.assertj.core.api.Assertions.assertThat(result.getBids()).hasSize(1);
            org.assertj.core.api.Assertions.assertThat(result.getBids().get(0).getBidAmount()).isEqualTo(10000L);
            org.assertj.core.api.Assertions.assertThat(result.getBids().get(0).getBidderNickname()).isEqualTo(bidder.getNickname());
        }

        @Test
        @DisplayName("입찰 판매글이 아닌 경우 예외가 발생해야 한다")
        void getBidHistory_NotAuctionFeed() {
            // given
            SalesType directSalesType = SalesType.builder()
                    .SalesTypeId(1L)
                    .name("직접 판매")
                    .build();
            
            TransactionFeed directFeed = TransactionFeed.builder()
                    .transactionFeedId(feedId)
                    .user(feed.getUser())
                    .title("데이터 판매")
                    .content("데이터 판매합니다")
                    .telecomCompany(telecomCompany)
                    .salesType(directSalesType)  // 직접 판매 타입
                    .salesPrice(5000L)
                    .salesDataAmount(1000L)
                    .defaultImageNumber(1L)
                    .expiresAt(LocalDateTime.now().plusDays(10))
                    .status(onSaleStatus)
                    .isDeleted(false)
                    .build();
            
            when(transactionFeedRepository.findById(feedId)).thenReturn(Optional.of(directFeed));
            when(salesTypeManager.getBidSaleType()).thenReturn(bidSalesType);

            // when & then
            BidException exception = assertThrows(BidException.class, () -> bidService.getBidHistory(feedId));
            org.assertj.core.api.Assertions.assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FEED_NOT_AUCTION);
        }
    }
}