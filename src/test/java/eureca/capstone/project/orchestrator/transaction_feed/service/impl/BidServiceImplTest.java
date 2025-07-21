package eureca.capstone.project.orchestrator.transaction_feed.service.impl;

import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.common.entity.TelecomCompany;
import eureca.capstone.project.orchestrator.common.exception.code.ErrorCode;
import eureca.capstone.project.orchestrator.common.exception.custom.BidException;
import eureca.capstone.project.orchestrator.common.util.SalesTypeManager;
import eureca.capstone.project.orchestrator.common.util.StatusManager;
import eureca.capstone.project.orchestrator.pay.service.UserPayService;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.PlaceBidRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.entity.Bids;
import eureca.capstone.project.orchestrator.transaction_feed.entity.SalesType;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.transaction_feed.repository.BidsRepository;
import eureca.capstone.project.orchestrator.transaction_feed.repository.custom.TransactionFeedRepositoryCustom;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BidServiceImplTest {

    @InjectMocks
    private BidServiceImpl bidService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionFeedRepositoryCustom transactionFeedRepositoryCustom;

    @Mock
    private BidsRepository bidsRepository;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private RedisScript<List> bidScript;

    @Mock
    private StatusManager statusManager;

    @Mock
    private SalesTypeManager salesTypeManager;

    @Mock
    private UserPayService userPayService;

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
        email = "test@example.com";
        bidAmount = 10000L;
        feedId = 1L;

        telecomCompany = TelecomCompany.builder()
                .telecomCompanyId(1L)
                .name("테스트통신사")
                .build();

        bidder = User.builder()
                .userId(1L)
                .email(email)
                .nickname("테스트유저")
                .telecomCompany(telecomCompany)
                .build();

        User seller = User.builder()
                .userId(2L)
                .email("seller@example.com")
                .nickname("판매자")
                .telecomCompany(telecomCompany)
                .build();

        bidSalesType = SalesType.builder()
                .SalesTypeId(2L)
                .name("입찰 판매")
                .build();

        onSaleStatus = Status.builder()
                .statusId(1L)
                .domain("FEED")
                .code("ON_SALE")
                .build();

        feed = TransactionFeed.builder()
                .transactionFeedId(feedId)
                .user(seller)
                .title("데이터 판매")
                .content("데이터 판매합니다")
                .telecomCompany(telecomCompany)
                .salesType(bidSalesType)
                .salesPrice(5000L)
                .salesDataAmount(1000L)
                .defaultImageNumber(1L)
                .expiresAt(LocalDateTime.now().plusDays(10))
                .status(onSaleStatus)
                .isDeleted(false)
                .build();
    }

    @Nested
    @DisplayName("입찰 기능")
    class PlaceBid {

        @Test
        @DisplayName("입찰 성공 시 입찰 내역이 저장되어야 한다")
        void placeBid_Success() {
            // given
            PlaceBidRequestDto request = new PlaceBidRequestDto();
            request.setTransactionFeedId(feedId);
            request.setBidAmount(bidAmount);

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(bidder));
            when(transactionFeedRepositoryCustom.findById(feedId)).thenReturn(Optional.of(feed));
            when(statusManager.getStatus("FEED", "ON_SALE")).thenReturn(onSaleStatus);
            when(salesTypeManager.getBidSaleType()).thenReturn(bidSalesType);

            List<String> mockResult = Arrays.asList("SUCCESS", "0", "0");
            when(stringRedisTemplate.execute(
                    any(RedisScript.class),
                    any(List.class),
                    anyString(),
                    anyString()
            )).thenReturn(mockResult);


            // when
            bidService.placeBid(email, request);

            // then
            verify(bidsRepository).save(any(Bids.class));
        }

        @Test
        @DisplayName("입찰 금액이 판매가보다 낮으면 예외가 발생해야 한다")
        void placeBid_BidAmountTooLow() {
            // given
            PlaceBidRequestDto request = new PlaceBidRequestDto();
            request.setTransactionFeedId(feedId);
            request.setBidAmount(1000L); // 판매가(5000L)보다 낮은 금액

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(bidder));
            when(transactionFeedRepositoryCustom.findById(feedId)).thenReturn(Optional.of(feed));
            when(statusManager.getStatus("FEED", "ON_SALE")).thenReturn(onSaleStatus);
            when(salesTypeManager.getBidSaleType()).thenReturn(bidSalesType);

            // when & then
            assertThrows(BidException.class, () -> bidService.placeBid(email, request));
        }
    }
    @Nested
    @DisplayName("입찰 내역 조회 기능")
    class GetBidHistory {

        @Test
        @DisplayName("입찰 내역 조회 성공 시 입찰 내역 목록이 반환되어야 한다")
        void getBidHistory_Success() {
            // given
            when(transactionFeedRepositoryCustom.findById(feedId)).thenReturn(Optional.of(feed));
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
            verify(transactionFeedRepositoryCustom).findById(feedId);
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
            
            when(transactionFeedRepositoryCustom.findById(feedId)).thenReturn(Optional.of(directFeed));
            when(salesTypeManager.getBidSaleType()).thenReturn(bidSalesType);

            // when & then
            BidException exception = assertThrows(BidException.class, () -> bidService.getBidHistory(feedId));
            org.assertj.core.api.Assertions.assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FEED_NOT_AUCTION);
        }
    }
}