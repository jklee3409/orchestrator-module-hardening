package eureca.capstone.project.orchestrator.transaction_feed.service.impl;

import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.common.entity.TelecomCompany;
import eureca.capstone.project.orchestrator.common.exception.custom.UserNotFoundException;
import eureca.capstone.project.orchestrator.common.util.StatusManager;
import eureca.capstone.project.orchestrator.transaction_feed.dto.UserDataCouponDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.GetUserDataCouponListResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.entity.DataCoupon;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.transaction_feed.entity.UserDataCoupon;
import eureca.capstone.project.orchestrator.transaction_feed.repository.DataCouponRepository;
import eureca.capstone.project.orchestrator.transaction_feed.repository.UserDataCouponRepository;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataCouponServiceImplTest {

    @Mock
    private DataCouponRepository dataCouponRepository;

    @Mock
    private UserDataCouponRepository userDataCouponRepository;

    @Mock
    private StatusManager statusManager;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DataCouponServiceImpl dataCouponService;

    private User buyer;
    private User user;
    private TransactionFeed transactionFeed;
    private DataCoupon dataCoupon;
    private Status issuedStatus;
    private Status status;
    private TelecomCompany telecomCompany;
    private TelecomCompany sktTelecom;
    private TelecomCompany ktTelecom;
    private UserDataCoupon userDataCoupon;

    @BeforeEach
    void setUp() {
        // 통신사 설정
        telecomCompany = TelecomCompany.builder()
                .telecomCompanyId(1L)
                .name("SKT")
                .build();

        sktTelecom = TelecomCompany.builder()
                .telecomCompanyId(1L)
                .name("SKT")
                .build();

        ktTelecom = TelecomCompany.builder()
                .telecomCompanyId(2L)
                .name("KT")
                .build();

        // 구매자 설정
        buyer = User.builder()
                .userId(1L)
                .email("buyer@example.com")
                .nickname("구매자")
                .telecomCompany(telecomCompany)
                .build();

        // 사용자 설정
        user = User.builder()
                .userId(1L)
                .email("user@example.com")
                .nickname("사용자")
                .telecomCompany(telecomCompany)
                .build();

        // 판매글 설정
        transactionFeed = TransactionFeed.builder()
                .transactionFeedId(1L)
                .user(User.builder().userId(2L).build()) // 판매자
                .title("데이터 판매")
                .content("1GB 데이터 판매합니다")
                .telecomCompany(telecomCompany)
                .salesDataAmount(1000L) // 1GB
                .salesPrice(10000L)
                .build();

        // 데이터 쿠폰 설정
        dataCoupon = DataCoupon.builder()
                .dataCouponId(1L)
                .dataAmount(1000L)
                .telecomCompany(telecomCompany)
                .couponNumber("test-coupon-number")
                .build();

        // 상태 설정
        issuedStatus = Status.builder()
                .statusId(1L)
                .domain("COUPON")
                .code("ISSUED")
                .description("발급됨")
                .build();

        status = Status.builder()
                .statusId(1L)
                .domain("COUPON")
                .code("ISSUED")
                .description("발급됨")
                .build();

        // 사용자 데이터 쿠폰 설정
        userDataCoupon = UserDataCoupon.builder()
                .userDataCouponId(1L)
                .user(user)
                .dataCoupon(dataCoupon)
                .status(status)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
    }

    @Test
    @DisplayName("기존 데이터 쿠폰이 있는 경우 데이터 쿠폰 발급 성공")
    void issueDataCoupon_WithExistingDataCoupon_Success() {
        // given
        when(dataCouponRepository.findByDataAmountAndTelecomCompany(anyLong(), any(TelecomCompany.class)))
                .thenReturn(Optional.of(dataCoupon));
        when(statusManager.getStatus(anyString(), anyString()))
                .thenReturn(issuedStatus);

        ArgumentCaptor<UserDataCoupon> userDataCouponCaptor = ArgumentCaptor.forClass(UserDataCoupon.class);

        // when
        dataCouponService.issueDataCoupon(buyer, transactionFeed);

        // then
        verify(dataCouponRepository).findByDataAmountAndTelecomCompany(
                transactionFeed.getSalesDataAmount(), transactionFeed.getTelecomCompany());
        verify(statusManager).getStatus("COUPON", "ISSUED");
        verify(userDataCouponRepository).save(userDataCouponCaptor.capture());

        UserDataCoupon savedUserDataCoupon = userDataCouponCaptor.getValue();
        assertThat(savedUserDataCoupon.getUser()).isEqualTo(buyer);
        assertThat(savedUserDataCoupon.getDataCoupon()).isEqualTo(dataCoupon);
        assertThat(savedUserDataCoupon.getStatus()).isEqualTo(issuedStatus);
        assertThat(savedUserDataCoupon.getExpiresAt()).isNotNull();
    }

    @Test
    @DisplayName("기존 데이터 쿠폰이 없는 경우 새 데이터 쿠폰 생성 후 발급 성공")
    void issueDataCoupon_WithNewDataCoupon_Success() {
        // given
        when(dataCouponRepository.findByDataAmountAndTelecomCompany(anyLong(), any(TelecomCompany.class)))
                .thenReturn(Optional.empty());
        when(dataCouponRepository.save(any(DataCoupon.class)))
                .thenReturn(dataCoupon);
        when(statusManager.getStatus(anyString(), anyString()))
                .thenReturn(issuedStatus);

        ArgumentCaptor<DataCoupon> dataCouponCaptor = ArgumentCaptor.forClass(DataCoupon.class);
        ArgumentCaptor<UserDataCoupon> userDataCouponCaptor = ArgumentCaptor.forClass(UserDataCoupon.class);

        // when
        dataCouponService.issueDataCoupon(buyer, transactionFeed);

        // then
        verify(dataCouponRepository).findByDataAmountAndTelecomCompany(
                transactionFeed.getSalesDataAmount(), transactionFeed.getTelecomCompany());
        verify(dataCouponRepository).save(dataCouponCaptor.capture());
        verify(statusManager).getStatus("COUPON", "ISSUED");
        verify(userDataCouponRepository).save(userDataCouponCaptor.capture());

        DataCoupon savedDataCoupon = dataCouponCaptor.getValue();
        assertThat(savedDataCoupon.getDataAmount()).isEqualTo(transactionFeed.getSalesDataAmount());
        assertThat(savedDataCoupon.getTelecomCompany()).isEqualTo(transactionFeed.getTelecomCompany());
        assertThat(savedDataCoupon.getCouponNumber()).isNotNull();

        UserDataCoupon savedUserDataCoupon = userDataCouponCaptor.getValue();
        assertThat(savedUserDataCoupon.getUser()).isEqualTo(buyer);
        assertThat(savedUserDataCoupon.getDataCoupon()).isEqualTo(dataCoupon);
        assertThat(savedUserDataCoupon.getStatus()).isEqualTo(issuedStatus);
        assertThat(savedUserDataCoupon.getExpiresAt()).isNotNull();
    }

    @Test
    @DisplayName("사용자 데이터 쿠폰 목록 조회 성공")
    void getUserDataCouponList_Success() {
        // given
        String email = "user@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userDataCouponRepository.findDetailsByUser(user)).thenReturn(List.of(userDataCoupon));

        // when
        GetUserDataCouponListResponseDto result = dataCouponService.getUserDataCouponList(email);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getDataCoupons()).hasSize(1);
        assertThat(result.getDataCoupons().get(0).getUserDataCouponId()).isEqualTo(userDataCoupon.getUserDataCouponId());
        assertThat(result.getDataCoupons().get(0).getCouponNumber()).isEqualTo(dataCoupon.getCouponNumber());
        assertThat(result.getDataCoupons().get(0).getDataAmount()).isEqualTo(dataCoupon.getDataAmount());
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 데이터 쿠폰 목록 조회 실패")
    void getUserDataCouponList_UserNotFound() {
        // given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when, then
        assertThrows(UserNotFoundException.class, () -> dataCouponService.getUserDataCouponList(email));
    }

    @Test
    @DisplayName("여러 개의 데이터 쿠폰을 가진 사용자 조회 성공")
    void getUserDataCouponList_WithMultipleCoupons_Success() {
        // given
        String email = "test@example.com";
        
        DataCoupon dataCoupon1 = DataCoupon.builder()
                .dataCouponId(1L)
                .dataAmount(1000L)
                .telecomCompany(sktTelecom)
                .couponNumber("coupon-number-1")
                .build();
                
        DataCoupon dataCoupon2 = DataCoupon.builder()
                .dataCouponId(2L)
                .dataAmount(2000L)
                .telecomCompany(ktTelecom)
                .couponNumber("coupon-number-2")
                .build();
                
        UserDataCoupon userDataCoupon1 = UserDataCoupon.builder()
                .userDataCouponId(1L)
                .user(user)
                .dataCoupon(dataCoupon1)
                .status(issuedStatus)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
                
        UserDataCoupon userDataCoupon2 = UserDataCoupon.builder()
                .userDataCouponId(2L)
                .user(user)
                .dataCoupon(dataCoupon2)
                .status(issuedStatus)
                .expiresAt(LocalDateTime.now().plusHours(48))
                .build();
                
        List<UserDataCoupon> userDataCoupons = List.of(userDataCoupon1, userDataCoupon2);
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userDataCouponRepository.findDetailsByUser(user)).thenReturn(userDataCoupons);

        // when
        GetUserDataCouponListResponseDto result = dataCouponService.getUserDataCouponList(email);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getDataCoupons()).hasSize(2);
        
        // 첫 번째 쿠폰 검증
        UserDataCouponDto firstCoupon = result.getDataCoupons().get(0);
        assertThat(firstCoupon.getUserDataCouponId()).isEqualTo(userDataCoupon1.getUserDataCouponId());
        assertThat(firstCoupon.getDataAmount()).isEqualTo(dataCoupon1.getDataAmount());
        assertThat(firstCoupon.getCouponNumber()).isEqualTo(dataCoupon1.getCouponNumber());
        
        // 두 번째 쿠폰 검증
        UserDataCouponDto secondCoupon = result.getDataCoupons().get(1);
        assertThat(secondCoupon.getUserDataCouponId()).isEqualTo(userDataCoupon2.getUserDataCouponId());
        assertThat(secondCoupon.getDataAmount()).isEqualTo(dataCoupon2.getDataAmount());
        assertThat(secondCoupon.getCouponNumber()).isEqualTo(dataCoupon2.getCouponNumber());
    }

    @Test
    @DisplayName("데이터 쿠폰이 없는 사용자 조회 성공")
    void getUserDataCouponList_WithNoCoupons_Success() {
        // given
        String email = "test@example.com";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userDataCouponRepository.findDetailsByUser(user)).thenReturn(new ArrayList<>());

        // when
        GetUserDataCouponListResponseDto result = dataCouponService.getUserDataCouponList(email);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getDataCoupons()).isEmpty();
    }
}