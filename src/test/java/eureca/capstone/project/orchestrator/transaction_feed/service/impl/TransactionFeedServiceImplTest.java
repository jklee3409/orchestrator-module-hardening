package eureca.capstone.project.orchestrator.transaction_feed.service.impl;

import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.common.entity.TelecomCompany;
import eureca.capstone.project.orchestrator.common.exception.custom.*;
import eureca.capstone.project.orchestrator.common.repository.TelecomCompanyRepository;
import eureca.capstone.project.orchestrator.common.util.StatusManager;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.CreateFeedRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.UpdateFeedRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.CreateFeedResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.GetFeedDetailResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.UpdateFeedResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.entity.SalesType;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.transaction_feed.repository.SalesTypeRepository;
import eureca.capstone.project.orchestrator.transaction_feed.repository.TransactionFeedRepository;
import eureca.capstone.project.orchestrator.transaction_feed.repository.custom.TransactionFeedRepositoryCustom;
import eureca.capstone.project.orchestrator.user.dto.response.user_data.DeductSellableDataResponseDto;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.entity.UserData;
import eureca.capstone.project.orchestrator.user.repository.UserDataRepository;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import eureca.capstone.project.orchestrator.user.repository.custom.UserDataRepositoryCustom;
import eureca.capstone.project.orchestrator.user.service.UserDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TransactionFeedServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDataRepository userDataRepository;

    @Mock
    private TelecomCompanyRepository telecomCompanyRepository;

    @Mock
    private SalesTypeRepository salesTypeRepository;

    @Mock
    private TransactionFeedRepository transactionFeedRepository;

    @Mock
    private UserDataService userDataService;

    @Mock
    private UserDataRepositoryCustom userDataRepositoryCustom;

    @Mock
    private TransactionFeedRepositoryCustom transactionFeedRepositoryCustom;

    @Mock
    private StatusManager statusManager;

    @InjectMocks
    private TransactionFeedServiceImpl transactionFeedService;

    private User user;
    private UserData userData;
    private TelecomCompany telecomCompany;
    private SalesType salesType;
    private Status status;
    private CreateFeedRequestDto createFeedRequestDto;
    private TransactionFeed transactionFeed;
    private DeductSellableDataResponseDto deductSellableDataResponseDto;

    @BeforeEach
    void setUp() {
        telecomCompany = TelecomCompany.builder()
                .telecomCompanyId(1L)
                .name("테스트 통신사")
                .build();

        status = mock(Status.class);
        when(status.getStatusId()).thenReturn(1L);
        when(status.getDomain()).thenReturn("FEED");
        when(status.getCode()).thenReturn("ON_SALE");

        salesType = new SalesType();
        try {
            java.lang.reflect.Field idField = SalesType.class.getDeclaredField("SalesTypeId");
            idField.setAccessible(true);
            idField.set(salesType, 1L);

            java.lang.reflect.Field nameField = SalesType.class.getDeclaredField("name");
            nameField.setAccessible(true);
            nameField.set(salesType, "일반판매");
        } catch (Exception e) {
            e.printStackTrace();
        }

        user = User.builder()
                .userId(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .nickname("테스트유저")
                .phoneNumber("01012345678")
                .provider("local")
                .telecomCompany(telecomCompany)
                .status(status)
                .build();

        userData = UserData.builder()
                .userDataId(1L)
                .userId(user.getUserId())
                .sellableDataMb(10000L)
                .resetDataAt(15)
                .build();

        createFeedRequestDto = CreateFeedRequestDto.builder()
                .title("테스트 피드")
                .content("테스트 내용")
                .telecomCompanyId(1L)
                .salesTypeId(1L)
                .salesPrice(10000L)
                .salesDataAmount(1000L)
                .defaultImageNumber(1L)
                .build();

        transactionFeed = TransactionFeed.builder()
                .transactionFeedId(1L)
                .user(user)
                .title("테스트 피드")
                .content("테스트 내용")
                .telecomCompany(telecomCompany)
                .salesType(salesType)
                .salesPrice(10000L)
                .salesDataAmount(1000L)
                .defaultImageNumber(1L)
                .expiresAt(LocalDateTime.now().plusDays(15))
                .status(status)
                .isDeleted(false)
                .build();

        try {
            java.lang.reflect.Field createdAtField = User.class.getSuperclass().getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(user, LocalDateTime.now());
        } catch (Exception e) {
            e.printStackTrace();
        }

        deductSellableDataResponseDto = DeductSellableDataResponseDto.builder()
                .userId(user.getUserId())
                .sellableDataMb(userData.getSellableDataMb() - createFeedRequestDto.getSalesDataAmount())
                .build();
    }

    @Test
    @DisplayName("피드 생성 성공")
    void createFeed_Success() {
        // Given
        String email = "test@example.com";

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(userDataRepositoryCustom.findByUserIdWithLock(anyLong())).thenReturn(Optional.of(userData));
        when(telecomCompanyRepository.findById(anyLong())).thenReturn(Optional.of(telecomCompany));
        when(salesTypeRepository.findById(anyLong())).thenReturn(Optional.of(salesType));
        when(statusManager.getStatus(anyString(), anyString())).thenReturn(status);

        when(transactionFeedRepository.save(any(TransactionFeed.class))).thenAnswer(new Answer<TransactionFeed>() {
            @Override
            public TransactionFeed answer(InvocationOnMock invocation) {
                TransactionFeed feed = invocation.getArgument(0);
                try {
                    java.lang.reflect.Field idField = TransactionFeed.class.getDeclaredField("transactionFeedId");
                    idField.setAccessible(true);
                    idField.set(feed, 1L);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return feed;
            }
        });

        when(userDataService.deductSellableData(anyLong(), anyLong())).thenReturn(deductSellableDataResponseDto);

        // When
        CreateFeedResponseDto responseDto = transactionFeedService.createFeed(email, createFeedRequestDto);

        // Then
        assertNotNull(responseDto);
        assertEquals(transactionFeed.getTransactionFeedId(), responseDto.getId());

        verify(userRepository).findByEmail(email);
        verify(userDataRepositoryCustom).findByUserIdWithLock(user.getUserId());
        verify(telecomCompanyRepository).findById(createFeedRequestDto.getTelecomCompanyId());
        verify(salesTypeRepository).findById(createFeedRequestDto.getSalesTypeId());
        verify(statusManager).getStatus("FEED", "ON_SALE");
        verify(transactionFeedRepository).save(any(TransactionFeed.class));
        verify(userDataService).deductSellableData(user.getUserId(), createFeedRequestDto.getSalesDataAmount());
    }

    @Test
    @DisplayName("피드 수정 성공")
    void updateFeed_Success() {
        // Given
        String email = "test@example.com";
        UpdateFeedRequestDto updateFeedRequestDto = UpdateFeedRequestDto.builder()
                .transactionFeedId(1L)
                .title("수정된 제목")
                .content("수정된 내용")
                .salesPrice(15000L)
                .salesDataAmount(800L)
                .defaultImageNumber(2L)
                .build();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(transactionFeedRepositoryCustom.findByIdWithLock(anyLong())).thenReturn(Optional.of(transactionFeed));

        // When
        UpdateFeedResponseDto responseDto = transactionFeedService.updateFeed(email, updateFeedRequestDto);

        // Then
        assertNotNull(responseDto);
        assertEquals(transactionFeed.getTransactionFeedId(), responseDto.getTransactionFeedId());
        assertEquals("수정된 제목", transactionFeed.getTitle());
        assertEquals("수정된 내용", transactionFeed.getContent());
        assertEquals(15000L, transactionFeed.getSalesPrice());
        assertEquals(800L, transactionFeed.getSalesDataAmount());
        assertEquals(2L, transactionFeed.getDefaultImageNumber());

        verify(userRepository).findByEmail(email);
        verify(transactionFeedRepositoryCustom).findByIdWithLock(updateFeedRequestDto.getTransactionFeedId());
        verify(userDataService).addSellableData(user.getUserId(), 200L); // 1000L - 800L = 200L 환불
    }

    @Test
    @DisplayName("다른 사용자의 피드 수정 시 예외 발생")
    void updateFeed_DifferentUser_ThrowsException() {
        // Given
        String email = "test@example.com";
        UpdateFeedRequestDto updateFeedRequestDto = UpdateFeedRequestDto.builder()
                .transactionFeedId(1L)
                .title("수정된 제목")
                .content("수정된 내용")
                .salesPrice(15000L)
                .salesDataAmount(800L)
                .defaultImageNumber(2L)
                .build();

        User differentUser = User.builder()
                .userId(2L)
                .email("different@example.com")
                .password("encodedPassword")
                .nickname("다른유저")
                .phoneNumber("01087654321")
                .provider("local")
                .telecomCompany(telecomCompany)
                .status(status)
                .build();

        TransactionFeed differentUserFeed = TransactionFeed.builder()
                .transactionFeedId(1L)
                .user(differentUser)
                .title("테스트 피드")
                .content("테스트 내용")
                .telecomCompany(telecomCompany)
                .salesType(salesType)
                .salesPrice(10000L)
                .salesDataAmount(1000L)
                .defaultImageNumber(1L)
                .expiresAt(LocalDateTime.now().plusDays(15))
                .status(status)
                .isDeleted(false)
                .build();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(transactionFeedRepositoryCustom.findByIdWithLock(anyLong())).thenReturn(Optional.of(differentUserFeed));

        // When & Then
        assertThrows(InternalServerException.class, () -> transactionFeedService.updateFeed(email, updateFeedRequestDto));

        verify(userRepository).findByEmail(email);
        verify(transactionFeedRepositoryCustom).findByIdWithLock(updateFeedRequestDto.getTransactionFeedId());
        verify(userDataService, never()).addSellableData(anyLong(), anyLong());
        verify(userDataService, never()).deductSellableData(anyLong(), anyLong());
    }

    @Test
    @DisplayName("일반 판매글 상세 조회 성공")
    void getFeedDetail_Success() {
        // Given
        Long transactionFeedId = 1L;

        when(transactionFeedRepositoryCustom.findFeedDetailById(transactionFeedId))
                .thenReturn(Optional.of(transactionFeed));

        // When
        GetFeedDetailResponseDto responseDto = transactionFeedService.getFeedDetail(transactionFeedId);

        // Then
        assertNotNull(responseDto);
        assertEquals(transactionFeed.getTransactionFeedId(), responseDto.getTransactionFeedId());
        assertEquals(transactionFeed.getTitle(), responseDto.getTitle());
        assertEquals(transactionFeed.getContent(), responseDto.getContent());
        assertEquals(transactionFeed.getSalesDataAmount(), responseDto.getSalesDataAmount());
        assertEquals(transactionFeed.getSalesPrice(), responseDto.getSalesPrice());
        assertEquals(transactionFeed.getDefaultImageNumber(), responseDto.getDefaultImageNumber());
        assertEquals(transactionFeed.getUser().getNickname(), responseDto.getNickname());
        assertNull(responseDto.getCurrentHeightPrice());

        verify(transactionFeedRepositoryCustom).findFeedDetailById(transactionFeedId);
    }

    @Test
    @DisplayName("입찰 판매글 상세 조회 성공")
    void getFeedDetail_AuctionType_Success() {
        // Given
        Long transactionFeedId = 1L;

        SalesType auctionSalesType = new SalesType();
        try {
            java.lang.reflect.Field idField = SalesType.class.getDeclaredField("SalesTypeId");
            idField.setAccessible(true);
            idField.set(auctionSalesType, 2L);

            java.lang.reflect.Field nameField = SalesType.class.getDeclaredField("name");
            nameField.setAccessible(true);
            nameField.set(auctionSalesType, "입찰 판매");
        } catch (Exception e) {
            e.printStackTrace();
        }

        TransactionFeed auctionFeed = TransactionFeed.builder()
                .transactionFeedId(1L)
                .user(user)
                .title("경매 피드")
                .content("경매 내용")
                .telecomCompany(telecomCompany)
                .salesType(auctionSalesType)
                .salesPrice(10000L)
                .salesDataAmount(1000L)
                .defaultImageNumber(1L)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .status(status)
                .isDeleted(false)
                .build();

        when(transactionFeedRepositoryCustom.findFeedDetailById(transactionFeedId))
                .thenReturn(Optional.of(auctionFeed));

        // When
        GetFeedDetailResponseDto responseDto = transactionFeedService.getFeedDetail(transactionFeedId);

        // Then
        assertNotNull(responseDto);
        assertEquals(auctionFeed.getTransactionFeedId(), responseDto.getTransactionFeedId());
        assertEquals(auctionFeed.getTitle(), responseDto.getTitle());
        assertEquals(auctionFeed.getContent(), responseDto.getContent());
        assertEquals(auctionFeed.getSalesDataAmount(), responseDto.getSalesDataAmount());
        assertEquals(auctionFeed.getSalesPrice(), responseDto.getSalesPrice());
        assertEquals(auctionFeed.getDefaultImageNumber(), responseDto.getDefaultImageNumber());
        assertEquals(auctionFeed.getUser().getNickname(), responseDto.getNickname());
        assertNotNull(responseDto.getCurrentHeightPrice());

        verify(transactionFeedRepositoryCustom).findFeedDetailById(transactionFeedId);
    }

    @Test
    @DisplayName("존재하지 않는 판매글 조회 시 예외 발생")
    void getFeedDetail_FeedNotFound_ThrowsException() {
        // Given
        Long nonExistentFeedId = 999L;

        when(transactionFeedRepositoryCustom.findFeedDetailById(nonExistentFeedId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(TransactionFeedNotFoundException.class, () -> transactionFeedService.getFeedDetail(nonExistentFeedId));

        verify(transactionFeedRepositoryCustom).findFeedDetailById(nonExistentFeedId);
    }
}
