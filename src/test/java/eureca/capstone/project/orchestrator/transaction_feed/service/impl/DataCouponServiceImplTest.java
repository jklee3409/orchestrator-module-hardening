package eureca.capstone.project.orchestrator.transaction_feed.service.impl;

import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.common.entity.TelecomCompany;
import eureca.capstone.project.orchestrator.common.util.StatusManager;
import eureca.capstone.project.orchestrator.transaction_feed.entity.DataCoupon;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.transaction_feed.entity.UserDataCoupon;
import eureca.capstone.project.orchestrator.transaction_feed.repository.DataCouponRepository;
import eureca.capstone.project.orchestrator.transaction_feed.repository.UserDataCouponRepository;
import eureca.capstone.project.orchestrator.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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

    @InjectMocks
    private DataCouponServiceImpl dataCouponService;

    private User buyer;
    private TransactionFeed transactionFeed;
    private DataCoupon dataCoupon;
    private Status issuedStatus;
    private TelecomCompany telecomCompany;

    @BeforeEach
    void setUp() {
        // 통신사 설정
        telecomCompany = TelecomCompany.builder()
                .telecomCompanyId(1L)
                .name("SKT")
                .build();

        // 구매자 설정
        buyer = User.builder()
                .userId(1L)
                .email("buyer@example.com")
                .nickname("구매자")
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
}