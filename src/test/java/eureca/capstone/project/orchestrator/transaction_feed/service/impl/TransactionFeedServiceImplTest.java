package eureca.capstone.project.orchestrator.transaction_feed.service.impl;


import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.common.entity.Status;

import eureca.capstone.project.orchestrator.common.entity.TelecomCompany;

import eureca.capstone.project.orchestrator.common.exception.custom.DataOverSellableAmountException;

import eureca.capstone.project.orchestrator.common.exception.custom.FeedModifyPermissionException;

import eureca.capstone.project.orchestrator.common.exception.custom.TransactionFeedNotFoundException;

import eureca.capstone.project.orchestrator.common.exception.custom.InternalServerException;


import eureca.capstone.project.orchestrator.common.repository.TelecomCompanyRepository;

import eureca.capstone.project.orchestrator.common.util.SalesTypeManager;

import eureca.capstone.project.orchestrator.common.util.StatusManager;

import eureca.capstone.project.orchestrator.market_statistics.repository.MarketStatisticsRepository;
import eureca.capstone.project.orchestrator.transaction_feed.document.TransactionFeedDocument;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.CreateFeedRequestDto;

import eureca.capstone.project.orchestrator.transaction_feed.dto.request.RemoveWishFeedsRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.UpdateFeedRequestDto;

import eureca.capstone.project.orchestrator.transaction_feed.dto.request.AddWishFeedRequestDto;

import eureca.capstone.project.orchestrator.transaction_feed.dto.response.CreateFeedResponseDto;

import eureca.capstone.project.orchestrator.transaction_feed.dto.response.GetFeedDetailResponseDto;

import eureca.capstone.project.orchestrator.transaction_feed.dto.response.UpdateFeedResponseDto;

import eureca.capstone.project.orchestrator.transaction_feed.entity.Bids;
import eureca.capstone.project.orchestrator.transaction_feed.entity.SalesType;

import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;

import eureca.capstone.project.orchestrator.transaction_feed.entity.Liked;

import eureca.capstone.project.orchestrator.transaction_feed.repository.BidsRepository;
import eureca.capstone.project.orchestrator.transaction_feed.repository.SalesTypeRepository;

import eureca.capstone.project.orchestrator.transaction_feed.repository.TransactionFeedRepository;

import eureca.capstone.project.orchestrator.transaction_feed.repository.LikedRepository;

import eureca.capstone.project.orchestrator.transaction_feed.repository.TransactionFeedSearchRepository;
import eureca.capstone.project.orchestrator.transaction_feed.repository.custom.TransactionFeedRepositoryCustom;

import eureca.capstone.project.orchestrator.transaction_feed.service.LikedService;
import eureca.capstone.project.orchestrator.user.entity.User;

import eureca.capstone.project.orchestrator.user.entity.UserData;

import eureca.capstone.project.orchestrator.user.repository.UserRepository;

import eureca.capstone.project.orchestrator.user.repository.custom.UserDataRepositoryCustom;

import eureca.capstone.project.orchestrator.user.service.UserDataService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.Nested;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;

import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;


import java.lang.reflect.Field;

import java.time.LocalDateTime;

import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.ArgumentMatchers.anyLong;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class TransactionFeedServiceImplTest {


    @InjectMocks
    private TransactionFeedServiceImpl transactionFeedService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDataRepositoryCustom userDataRepositoryCustom;

    @Mock
    private TelecomCompanyRepository telecomCompanyRepository;

    @Mock
    private SalesTypeRepository salesTypeRepository;

    @Mock
    private TransactionFeedRepository transactionFeedRepository;

    @Mock
    private BidsRepository bidsRepository;

    @Mock
    private MarketStatisticsRepository marketStatisticsRepository;

    @Mock
    private UserDataService userDataService;

    @Mock
    private StatusManager statusManager;

    @Mock
    private SalesTypeManager salesTypeManager;

    @Mock
    private TransactionFeedSearchRepository transactionFeedSearchRepository;

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @Mock
    private IndexOperations indexOperations;

    @Mock
    private LikedRepository likedRepository;

    private User user;

    private UserData userData;

    private TelecomCompany telecomCompany;

    private SalesType normalSaleType;

    private SalesType bidSaleType;

    private Status onSaleStatus;

    private TransactionFeed transactionFeed;

    private CustomUserDetailsDto userDetailsDto;

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

        userData = UserData.builder()
                .userDataId(1L)
                .userId(user.getUserId())
                .sellableDataMb(10000L)
                .resetDataAt(20)
                .build();

        normalSaleType = SalesType.builder()
                .SalesTypeId(1L)
                .name("일반판매")
                .build();

        bidSaleType = SalesType.builder()
                .SalesTypeId(2L)
                .name("입찰 판매")
                .build();

        onSaleStatus = Status.builder()
                .statusId(1L)
                .domain("FEED")
                .code("ON_SALE")
                .build();

        transactionFeed = TransactionFeed.builder()
                .transactionFeedId(1L)
                .user(user)
                .title("기존 제목")
                .content("기존 내용")
                .telecomCompany(telecomCompany)
                .salesType(normalSaleType)
                .salesPrice(10000L)
                .salesDataAmount(1000L)
                .defaultImageNumber(1L)
                .expiresAt(LocalDateTime.now().plusDays(10))
                .status(onSaleStatus)
                .isDeleted(false)
                .build();

        userDetailsDto = CustomUserDetailsDto.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .build();

    }


    @Nested

    @DisplayName("판매글 생성")
    class CreateFeed {


        @Test
        @DisplayName("일반 판매글 생성 성공")
        void createFeed_Success() throws NoSuchFieldException, IllegalAccessException {

            // given
            CreateFeedRequestDto request = CreateFeedRequestDto.builder()
                    .title("팝니다")
                    .content("데이터 1GB 팝니다")
                    .telecomCompanyId(1L)
                    .salesTypeId(1L)
                    .salesPrice(10000L)
                    .salesDataAmount(1000L)
                    .defaultImageNumber(1L)
                    .build();

            when(salesTypeRepository.findById(anyLong())).thenReturn(Optional.of(normalSaleType));
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
            when(userDataRepositoryCustom.findByUserIdWithLock(anyLong())).thenReturn(Optional.of(userData));

            when(telecomCompanyRepository.findById(anyLong())).thenReturn(Optional.of(telecomCompany));
            when(statusManager.getStatus(anyString(), anyString())).thenReturn(onSaleStatus);

            when(salesTypeManager.getBidSaleType()).thenReturn(bidSaleType);

            doAnswer(invocation -> {
                TransactionFeed feedToSave = invocation.getArgument(0);

                Field idField = TransactionFeed.class.getDeclaredField("transactionFeedId");
                idField.setAccessible(true);
                idField.set(feedToSave, 1L);

                return feedToSave;

            }).when(transactionFeedRepository).save(any(TransactionFeed.class));

            // when
            CreateFeedResponseDto response = transactionFeedService.createFeed(user.getEmail(), request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);

            verify(salesTypeRepository).findById(request.getSalesTypeId());
            verify(userRepository).findByEmail(user.getEmail());
            verify(userDataRepositoryCustom).findByUserIdWithLock(user.getUserId());
            verify(userDataService).deductSellableData(user.getUserId(), request.getSalesDataAmount());
            verify(transactionFeedRepository).save(any(TransactionFeed.class));
        }


        @Test
        @DisplayName("판매 가능 데이터 초과시 예외 발생")
        void createFeed_ThrowsDataOverSellableAmountException() {
            // given
            CreateFeedRequestDto request = CreateFeedRequestDto.builder()
                    .salesDataAmount(20000L)
                    .salesTypeId(1L)
                    .build();

            when(salesTypeRepository.findById(anyLong())).thenReturn(Optional.of(normalSaleType));
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
            when(userDataRepositoryCustom.findByUserIdWithLock(anyLong())).thenReturn(Optional.of(userData));
            when(salesTypeManager.getBidSaleType()).thenReturn(bidSaleType);

            // when & then
            assertThrows(DataOverSellableAmountException.class,
                    () -> transactionFeedService.createFeed(user.getEmail(), request));

            verify(transactionFeedRepository, never()).save(any(TransactionFeed.class));
            verify(userDataService, never()).deductSellableData(anyLong(), anyLong());

        }

    }


    @Nested
    @DisplayName("판매글 수정")
    class UpdateFeed {

        @Test
        @DisplayName("판매 데이터가 증가하는 경우 성공적으로 수정")
        void updateFeed_Success_IncreaseData() {

            // given
            UpdateFeedRequestDto request = UpdateFeedRequestDto.builder()
                    .transactionFeedId(1L)
                    .title("수정된 제목")
                    .content("수정된 내용")
                    .salesPrice(12000L)
                    .salesDataAmount(1500L) // 500 증가
                    .defaultImageNumber(2L)
                    .build();

            long dataChangeAmount = request.getSalesDataAmount() - transactionFeed.getSalesDataAmount();

            TransactionFeedDocument mockDocument = TransactionFeedDocument.builder()
                    .id(transactionFeed.getTransactionFeedId())
                    .title(transactionFeed.getTitle())
                    .content(transactionFeed.getContent())
                    .salesDataAmount(transactionFeed.getSalesDataAmount())
                    .defaultImageNumber(transactionFeed.getDefaultImageNumber())
                    .build();


            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
            when(transactionFeedRepository.findById(anyLong())).thenReturn(Optional.of(transactionFeed));
            when(userDataRepositoryCustom.findByUserIdWithLock(anyLong())).thenReturn(Optional.of(userData));
            when(salesTypeManager.getBidSaleType()).thenReturn(bidSaleType);
            when(transactionFeedSearchRepository.findById(anyLong())).thenReturn(Optional.of(mockDocument));
            when(statusManager.getStatus(anyString(), anyString())).thenReturn(onSaleStatus);

            // when
            UpdateFeedResponseDto response = transactionFeedService.updateFeed(user.getEmail(), request);

            // then
            assertThat(response.getTransactionFeedId()).isEqualTo(transactionFeed.getTransactionFeedId());
            assertThat(transactionFeed.getTitle()).isEqualTo(request.getTitle());
            assertThat(transactionFeed.getSalesDataAmount()).isEqualTo(request.getSalesDataAmount());

            verify(userDataService).deductSellableData(user.getUserId(), dataChangeAmount);
            verify(userDataService, never()).addSellableData(anyLong(), anyLong());
        }


        @Test

        @DisplayName("판매 데이터가 감소하는 경우 성공적으로 수정")
        void updateFeed_Success_DecreaseData() {

            // given
            UpdateFeedRequestDto request = UpdateFeedRequestDto.builder()
                    .transactionFeedId(1L)
                    .title("수정된 제목")
                    .content("수정된 내용")
                    .salesPrice(8000L)
                    .salesDataAmount(500L) // 500 감소
                    .defaultImageNumber(2L)
                    .build();

            long dataChangeAmount = transactionFeed.getSalesDataAmount() - request.getSalesDataAmount();

            TransactionFeedDocument mockDocument = TransactionFeedDocument.builder()
                    .id(transactionFeed.getTransactionFeedId())
                    .title(transactionFeed.getTitle())
                    .content(transactionFeed.getContent())
                    .salesDataAmount(transactionFeed.getSalesDataAmount())
                    .defaultImageNumber(transactionFeed.getDefaultImageNumber())
                    .build();

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
            when(transactionFeedRepository.findById(anyLong())).thenReturn(Optional.of(transactionFeed));
            when(salesTypeManager.getBidSaleType()).thenReturn(bidSaleType);
            when(transactionFeedSearchRepository.findById(anyLong())).thenReturn(Optional.of(mockDocument));
            when(statusManager.getStatus(anyString(), anyString())).thenReturn(onSaleStatus);

            // when
            UpdateFeedResponseDto response = transactionFeedService.updateFeed(user.getEmail(), request);

            // then
            assertThat(response.getTransactionFeedId()).isEqualTo(transactionFeed.getTransactionFeedId());
            assertThat(transactionFeed.getTitle()).isEqualTo(request.getTitle());
            assertThat(transactionFeed.getSalesDataAmount()).isEqualTo(request.getSalesDataAmount());

            verify(userDataService).addSellableData(user.getUserId(), dataChangeAmount);
            verify(userDataService, never()).deductSellableData(anyLong(), anyLong());
        }


        @Test
        @DisplayName("판매 데이터 변경이 없는 경우 성공적으로 수정")
        void updateFeed_Success_NoDataChange() {

            // given
            UpdateFeedRequestDto request = UpdateFeedRequestDto.builder()
                    .transactionFeedId(1L)
                    .title("제목만 수정")
                    .content("내용만 수정")
                    .salesPrice(10000L)
                    .salesDataAmount(transactionFeed.getSalesDataAmount()) // 변경 없음
                    .defaultImageNumber(1L)
                    .build();

            TransactionFeedDocument mockDocument = TransactionFeedDocument.builder()
                    .id(transactionFeed.getTransactionFeedId())
                    .title(transactionFeed.getTitle())
                    .content(transactionFeed.getContent())
                    .salesDataAmount(transactionFeed.getSalesDataAmount())
                    .defaultImageNumber(transactionFeed.getDefaultImageNumber())
                    .build();


            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
            when(transactionFeedRepository.findById(anyLong())).thenReturn(Optional.of(transactionFeed));
            when(salesTypeManager.getBidSaleType()).thenReturn(bidSaleType);
            when(transactionFeedSearchRepository.findById(anyLong())).thenReturn(Optional.of(mockDocument));
            when(statusManager.getStatus(anyString(), anyString())).thenReturn(onSaleStatus);

            // when
            transactionFeedService.updateFeed(user.getEmail(), request);

            // then
            verify(userDataService, never()).addSellableData(anyLong(), anyLong());
            verify(userDataService, never()).deductSellableData(anyLong(), anyLong());
        }


        @Test
        @DisplayName("작성자가 아닌 사용자가 수정 시도 시 예외 발생")
        void updateFeed_ThrowsFeedModifyPermissionException() {

            // given
            UpdateFeedRequestDto request = UpdateFeedRequestDto.builder().transactionFeedId(1L).build();
            User otherUser = User.builder().userId(2L).email("other@example.com").build();
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(otherUser));
            when(transactionFeedRepository.findById(anyLong())).thenReturn(Optional.of(transactionFeed));

            // when & then
            assertThrows(FeedModifyPermissionException.class,
                    () -> transactionFeedService.updateFeed(otherUser.getEmail(), request));
        }
    }


    @Nested
    @DisplayName("판매글 조회 및 삭제")
    class ReadAndDeleteFeed {

        @Test
        @DisplayName("판매글 상세 조회 성공")
        void getFeedDetail_Success() {
            // given
            Long feedId = 1L;
            when(transactionFeedRepository.findFeedDetailById(feedId)).thenReturn(Optional.of(transactionFeed));
            when(salesTypeManager.getBidSaleType()).thenReturn(bidSaleType);
            when(userRepository.findByEmail(userDetailsDto.getEmail())).thenReturn(Optional.of(user));
            when(likedRepository.existsByFeedAndUser(transactionFeed, user)).thenReturn(false);
            when(likedRepository.countByTransactionFeed(transactionFeed)).thenReturn(0L);


            // when
            GetFeedDetailResponseDto response = transactionFeedService.getFeedDetail(feedId, userDetailsDto);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getTransactionFeedId()).isEqualTo(transactionFeed.getTransactionFeedId());
            assertThat(response.getTitle()).isEqualTo(transactionFeed.getTitle());
            assertThat(response.getNickname()).isEqualTo(user.getNickname());
            assertThat(response.getCurrentHeightPrice()).isNull();
        }

        @Test
        @DisplayName("입찰 판매글 상세 조회는 DB 커밋 최고가를 사용한다")
        void getFeedDetail_AuctionUsesCommittedHighestBidFromDb() {
            // given
            Long feedId = 1L;
            Bids highestBid = mock(Bids.class);

            transactionFeed = TransactionFeed.builder()
                    .transactionFeedId(1L)
                    .user(user)
                    .title("입찰 판매글")
                    .content("입찰 내용")
                    .telecomCompany(telecomCompany)
                    .salesType(bidSaleType)
                    .salesPrice(10000L)
                    .salesDataAmount(1000L)
                    .defaultImageNumber(1L)
                    .expiresAt(LocalDateTime.now().plusDays(1))
                    .status(onSaleStatus)
                    .isDeleted(false)
                    .build();

            when(highestBid.getBidAmount()).thenReturn(13_500L);
            when(transactionFeedRepository.findFeedDetailById(feedId)).thenReturn(Optional.of(transactionFeed));
            when(salesTypeManager.getBidSaleType()).thenReturn(bidSaleType);
            when(userRepository.findByEmail(userDetailsDto.getEmail())).thenReturn(Optional.of(user));
            when(likedRepository.existsByFeedAndUser(transactionFeed, user)).thenReturn(false);
            when(likedRepository.countByTransactionFeed(transactionFeed)).thenReturn(0L);
            when(bidsRepository.findTopByTransactionFeedOrderByBidAmountDescBidTimeDesc(transactionFeed))
                    .thenReturn(Optional.of(highestBid));

            // when
            GetFeedDetailResponseDto response = transactionFeedService.getFeedDetail(feedId, userDetailsDto);

            // then
            assertThat(response.getCurrentHeightPrice()).isEqualTo(13_500L);
        }

        @Test
        @DisplayName("재색인은 DB 최고가를 기준으로 ES 문서를 복원한다")
        void reindexAllFeeds_UsesCommittedHighestBidFromDb() {
            // given
            Long feedId = 1L;
            transactionFeed = TransactionFeed.builder()
                    .transactionFeedId(feedId)
                    .user(user)
                    .title("입찰 판매글")
                    .content("입찰 내용")
                    .telecomCompany(telecomCompany)
                    .salesType(bidSaleType)
                    .salesPrice(10000L)
                    .salesDataAmount(1000L)
                    .defaultImageNumber(1L)
                    .expiresAt(LocalDateTime.now().plusDays(1))
                    .status(onSaleStatus)
                    .isDeleted(false)
                    .build();

            when(elasticsearchOperations.indexOps(TransactionFeedDocument.class)).thenReturn(indexOperations);
            when(indexOperations.exists()).thenReturn(false);
            when(transactionFeedRepository.findAll()).thenReturn(List.of(transactionFeed));
            when(salesTypeManager.getBidSaleType()).thenReturn(bidSaleType);
            when(bidsRepository.findHighestBidAmountsByTransactionFeedIds(List.of(feedId)))
                    .thenReturn(Map.of(feedId, 14_000L));

            // when
            long reindexedCount = transactionFeedService.reindexAllFeeds();

            // then
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Iterable<TransactionFeedDocument>> documentsCaptor =
                    ArgumentCaptor.forClass(Iterable.class);
            verify(transactionFeedSearchRepository).saveAll(documentsCaptor.capture());

            TransactionFeedDocument savedDocument = documentsCaptor.getValue().iterator().next();
            assertThat(reindexedCount).isEqualTo(1L);
            assertThat(savedDocument.getCurrentHighestPrice()).isEqualTo(14_000L);
            assertThat(savedDocument.getSortPrice()).isEqualTo(14_000L);
        }


        @Test
        @DisplayName("존재하지 않는 판매글 조회 시 예외 발생")
        void getFeedDetail_ThrowsTransactionFeedNotFoundException() {
            // given
            Long nonExistentFeedId = 999L;
            when(transactionFeedRepository.findFeedDetailById(nonExistentFeedId)).thenReturn(Optional.empty());

            // when & then
            assertThrows(TransactionFeedNotFoundException.class,
                    () -> transactionFeedService.getFeedDetail(nonExistentFeedId, userDetailsDto));
        }

        @Test
        @DisplayName("판매글 삭제 성공")
        void deleteFeed_Success() {
            // given
            String email = user.getEmail();
            Long feedId = transactionFeed.getTransactionFeedId();
            when(transactionFeedRepository.findById(feedId)).thenReturn(Optional.of(transactionFeed));
            when(salesTypeManager.getBidSaleType()).thenReturn(bidSaleType);

            // when
            transactionFeedService.deleteFeed(email, feedId);

            // then
            assertTrue(transactionFeed.isDeleted());
            verify(transactionFeedRepository).findById(feedId);
        }


        @Test

        @DisplayName("작성자가 아닌 사용자가 삭제 시도 시 예외 발생")
        void deleteFeed_ThrowsFeedModifyPermissionException() {
            // given
            Long feedId = transactionFeed.getTransactionFeedId();
            String otherUserEmail = "other@example.com";
            when(transactionFeedRepository.findById(feedId)).thenReturn(Optional.of(transactionFeed));

            // when & then
            assertThrows(FeedModifyPermissionException.class,
                    () -> transactionFeedService.deleteFeed(otherUserEmail, feedId));
            assertFalse(transactionFeed.isDeleted());
        }
    }
}
