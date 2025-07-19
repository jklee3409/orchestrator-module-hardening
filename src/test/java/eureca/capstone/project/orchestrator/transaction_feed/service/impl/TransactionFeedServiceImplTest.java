package eureca.capstone.project.orchestrator.transaction_feed.service.impl;


import eureca.capstone.project.orchestrator.common.entity.Status;

import eureca.capstone.project.orchestrator.common.entity.TelecomCompany;

import eureca.capstone.project.orchestrator.common.exception.custom.DataOverSellableAmountException;

import eureca.capstone.project.orchestrator.common.exception.custom.FeedModifyPermissionException;

import eureca.capstone.project.orchestrator.common.exception.custom.TransactionFeedNotFoundException;

import eureca.capstone.project.orchestrator.common.exception.custom.InternalServerException;

import eureca.capstone.project.orchestrator.common.exception.code.ErrorCode;

import eureca.capstone.project.orchestrator.common.repository.TelecomCompanyRepository;

import eureca.capstone.project.orchestrator.common.util.SalesTypeManager;

import eureca.capstone.project.orchestrator.common.util.StatusManager;

import eureca.capstone.project.orchestrator.transaction_feed.dto.request.CreateFeedRequestDto;

import eureca.capstone.project.orchestrator.transaction_feed.dto.request.UpdateFeedRequestDto;

import eureca.capstone.project.orchestrator.transaction_feed.dto.request.AddWishFeedRequestDto;

import eureca.capstone.project.orchestrator.transaction_feed.dto.response.CreateFeedResponseDto;

import eureca.capstone.project.orchestrator.transaction_feed.dto.response.GetFeedDetailResponseDto;

import eureca.capstone.project.orchestrator.transaction_feed.dto.response.UpdateFeedResponseDto;

import eureca.capstone.project.orchestrator.transaction_feed.entity.SalesType;

import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;

import eureca.capstone.project.orchestrator.transaction_feed.entity.Liked;

import eureca.capstone.project.orchestrator.transaction_feed.repository.SalesTypeRepository;

import eureca.capstone.project.orchestrator.transaction_feed.repository.TransactionFeedRepository;

import eureca.capstone.project.orchestrator.transaction_feed.repository.LikedRepository;

import eureca.capstone.project.orchestrator.transaction_feed.repository.TransactionFeedSearchRepository;
import eureca.capstone.project.orchestrator.transaction_feed.repository.custom.TransactionFeedRepositoryCustom;

import eureca.capstone.project.orchestrator.user.entity.User;

import eureca.capstone.project.orchestrator.user.entity.UserData;

import eureca.capstone.project.orchestrator.user.repository.UserRepository;

import eureca.capstone.project.orchestrator.user.repository.custom.UserDataRepositoryCustom;

import eureca.capstone.project.orchestrator.user.service.UserDataService;

import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.Nested;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;

import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;


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
    private TransactionFeedRepositoryCustom transactionFeedRepositoryCustom;

    @Mock
    private UserDataService userDataService;

    @Mock
    private StatusManager statusManager;

    @Mock
    private SalesTypeManager salesTypeManager;

    @Mock
    private TransactionFeedSearchRepository transactionFeedSearchRepository;

    @Mock
    private LikedRepository likedRepository;

    private User user;

    private UserData userData;

    private TelecomCompany telecomCompany;

    private SalesType normalSaleType;

    private SalesType bidSaleType;

    private Status onSaleStatus;

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

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
            when(transactionFeedRepositoryCustom.findById(anyLong())).thenReturn(Optional.of(transactionFeed));
            when(userDataRepositoryCustom.findByUserIdWithLock(anyLong())).thenReturn(Optional.of(userData));
            when(salesTypeManager.getBidSaleType()).thenReturn(bidSaleType);

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

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
            when(transactionFeedRepositoryCustom.findById(anyLong())).thenReturn(Optional.of(transactionFeed));
            when(salesTypeManager.getBidSaleType()).thenReturn(bidSaleType);

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

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
            when(transactionFeedRepositoryCustom.findById(anyLong())).thenReturn(Optional.of(transactionFeed));
            when(salesTypeManager.getBidSaleType()).thenReturn(bidSaleType);

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
            when(transactionFeedRepositoryCustom.findById(anyLong())).thenReturn(Optional.of(transactionFeed));

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
            when(transactionFeedRepositoryCustom.findFeedDetailById(feedId)).thenReturn(Optional.of(transactionFeed));
            when(salesTypeManager.getBidSaleType()).thenReturn(bidSaleType);

            // when
            GetFeedDetailResponseDto response = transactionFeedService.getFeedDetail(feedId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getTransactionFeedId()).isEqualTo(transactionFeed.getTransactionFeedId());
            assertThat(response.getTitle()).isEqualTo(transactionFeed.getTitle());
            assertThat(response.getNickname()).isEqualTo(user.getNickname());
            assertThat(response.getCurrentHeightPrice()).isNull();
        }


        @Test
        @DisplayName("존재하지 않는 판매글 조회 시 예외 발생")
        void getFeedDetail_ThrowsTransactionFeedNotFoundException() {
            // given
            Long nonExistentFeedId = 999L;
            when(transactionFeedRepositoryCustom.findFeedDetailById(nonExistentFeedId)).thenReturn(Optional.empty());

            // when & then
            assertThrows(TransactionFeedNotFoundException.class,
                    () -> transactionFeedService.getFeedDetail(nonExistentFeedId));
        }

        @Test
        @DisplayName("판매글 삭제 성공")
        void deleteFeed_Success() {
            // given
            String email = user.getEmail();
            Long feedId = transactionFeed.getTransactionFeedId();
            when(transactionFeedRepositoryCustom.findById(feedId)).thenReturn(Optional.of(transactionFeed));
            when(salesTypeManager.getBidSaleType()).thenReturn(bidSaleType);

            // when
            transactionFeedService.deleteFeed(email, feedId);

            // then
            assertTrue(transactionFeed.isDeleted());
            verify(transactionFeedRepositoryCustom).findById(feedId);
        }


        @Test

        @DisplayName("작성자가 아닌 사용자가 삭제 시도 시 예외 발생")
        void deleteFeed_ThrowsFeedModifyPermissionException() {
            // given
            Long feedId = transactionFeed.getTransactionFeedId();
            String otherUserEmail = "other@example.com";
            when(transactionFeedRepositoryCustom.findById(feedId)).thenReturn(Optional.of(transactionFeed));

            // when & then
            assertThrows(FeedModifyPermissionException.class,
                    () -> transactionFeedService.deleteFeed(otherUserEmail, feedId));
            assertFalse(transactionFeed.isDeleted());
        }
    }


    @Nested
    @DisplayName("찜 목록 관리")
    class WishFeedOperations {

        @Test
        @DisplayName("찜 목록 추가 성공")
        void addWishFeed_Success() {
            // given
            String email = user.getEmail();
            AddWishFeedRequestDto requestDto = new AddWishFeedRequestDto();
            requestDto.setTransactionFeedId(1L);

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
            when(transactionFeedRepositoryCustom.findById(requestDto.getTransactionFeedId())).thenReturn(
                    Optional.of(transactionFeed));
            when(likedRepository.existsByFeedAndUser(transactionFeed, user)).thenReturn(false);

            // when
            transactionFeedService.addWishFeed(email, requestDto);

            // then
            verify(likedRepository).save(any(Liked.class));
        }

        @Test
        @DisplayName("이미 찜한 피드 추가 시 예외 발생")
        void addWishFeed_AlreadyExists_ThrowsException() {
            // given
            String email = user.getEmail();
            AddWishFeedRequestDto requestDto = new AddWishFeedRequestDto();
            requestDto.setTransactionFeedId(1L);

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
            when(transactionFeedRepositoryCustom.findById(requestDto.getTransactionFeedId())).thenReturn(
                    Optional.of(transactionFeed));
            when(likedRepository.existsByFeedAndUser(transactionFeed, user)).thenReturn(true);

            // when & then
            assertThrows(InternalServerException.class,
                    () -> transactionFeedService.addWishFeed(email, requestDto));

            verify(likedRepository, never()).save(any(Liked.class));
        }

        @Test
        @DisplayName("찜 목록 삭제 성공")
        void removeWishFeed_Success() {
            // given
            String email = user.getEmail();
            Long feedId = transactionFeed.getTransactionFeedId();

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
            when(transactionFeedRepositoryCustom.findById(feedId)).thenReturn(Optional.of(transactionFeed));
            when(likedRepository.existsByFeedAndUser(transactionFeed, user)).thenReturn(true);

            // when
            transactionFeedService.removeWishFeed(email, feedId);

            // then
            verify(likedRepository).removeByFeedAndUser(transactionFeed, user);
        }

        @Test
        @DisplayName("찜 목록에 없는 피드 삭제 시 예외 발생")
        void removeWishFeed_NotFound_ThrowsException() {
            // given
            String email = user.getEmail();
            Long feedId = transactionFeed.getTransactionFeedId();

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
            when(transactionFeedRepositoryCustom.findById(feedId)).thenReturn(Optional.of(transactionFeed));
            when(likedRepository.existsByFeedAndUser(transactionFeed, user)).thenReturn(false);

            // when & then
            assertThrows(InternalServerException.class,
                    () -> transactionFeedService.removeWishFeed(email, feedId));

            verify(likedRepository, never()).removeByFeedAndUser(any(), any());
        }
    }

}