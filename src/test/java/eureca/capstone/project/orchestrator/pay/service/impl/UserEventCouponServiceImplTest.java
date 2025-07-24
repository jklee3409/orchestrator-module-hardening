package eureca.capstone.project.orchestrator.pay.service.impl;

import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.common.exception.custom.EventCouponNotFoundException;
import eureca.capstone.project.orchestrator.common.exception.custom.UserEventCouponAlreadyExistsException;
import eureca.capstone.project.orchestrator.common.exception.custom.UserNotFoundException;
import eureca.capstone.project.orchestrator.common.util.StatusManager;
import eureca.capstone.project.orchestrator.pay.dto.response.IssuedCouponResponseDto;
import eureca.capstone.project.orchestrator.pay.entity.EventCoupon;
import eureca.capstone.project.orchestrator.pay.entity.UserEventCoupon;
import eureca.capstone.project.orchestrator.pay.repository.EventCouponRepository;
import eureca.capstone.project.orchestrator.pay.repository.UserEventCouponRepository;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserEventCouponServiceImplTest {
    @Mock
    UserRepository userRepository;
    @Mock
    EventCouponRepository eventCouponRepository;
    @Mock
    UserEventCouponRepository userEventCouponRepository;
    @Mock
    StatusManager statusManager;

    @InjectMocks
    private UserEventCouponServiceImpl couponService;

    private User testUser;
    private EventCoupon testEventCoupon;
    private Status issuedStatus;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(1L)
                .email("test@example.com")
                .build();

        testEventCoupon = EventCoupon.builder()
                .eventCouponId(10L)
                .build();

        issuedStatus = Status.builder()
                .statusId(1L)
                .domain("COUPON")
                .code("ISSUED")
                .description("발급됨")
                .build();
    }

    @Test
    @DisplayName("이벤트 쿠폰 발급 성공")
    void issueEventCoupon_Success() {
        // Given
        Long couponId = testEventCoupon.getEventCouponId();
        String email = testUser.getEmail();


        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(eventCouponRepository.findById(couponId)).thenReturn(Optional.of(testEventCoupon));
        when(userEventCouponRepository.existsByUserAndEventCoupon(testUser, testEventCoupon)).thenReturn(false);
        when(statusManager.getStatus("COUPON", "ISSUED")).thenReturn(issuedStatus);

        UserEventCoupon expectedUserEventCoupon = UserEventCoupon.builder()
                .user(testUser)
                .eventCoupon(testEventCoupon)
                .status(issuedStatus)
                .expiresAt(LocalDateTime.now().plusMonths(1))
                .build();

        when(userEventCouponRepository.save(Mockito.<UserEventCoupon>any())).thenAnswer(invocation -> {
            UserEventCoupon savedCoupon = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedCoupon, "userEventCouponId", 100L);
            return savedCoupon;
        });

        // When
        IssuedCouponResponseDto responseDto = couponService.issueEventCoupon(couponId, email);

        // Then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getId()).isEqualTo(100L);

        verify(userRepository, times(1)).findByEmail(email);
        verify(eventCouponRepository, times(1)).findById(couponId);
        verify(userEventCouponRepository, times(1)).existsByUserAndEventCoupon(testUser, testEventCoupon);
        verify(statusManager, times(1)).getStatus("COUPON", "ISSUED");
        verify(userEventCouponRepository, times(1))
                .save(Mockito.<UserEventCoupon>any());
    }

    @Test
    @DisplayName("이벤트 쿠폰 발급 실패_UserNotFound")
    void issueEventCoupon_UserNotFound() {
        // Given
        Long couponId = testEventCoupon.getEventCouponId();
        String email = "nonexistent@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> {
            couponService.issueEventCoupon(couponId, email);
        });

        verify(userRepository, times(1)).findByEmail(email); // findByEmail은 호출됨
        verify(eventCouponRepository, never()).findById(anyLong());
        verify(userEventCouponRepository, never()).existsByUserAndEventCoupon(any(), any());
        verify(statusManager, never()).getStatus(anyString(), anyString());
        verify(userEventCouponRepository, never()).save(any(UserEventCoupon.class));
    }

    @Test
    @DisplayName("이벤트 쿠폰 발급 실패_EventCouponNotFound")
    void issueEventCoupon_EventCouponNotFound() {
        // Given
        Long couponId = 999L; // 존재하지 않는 쿠폰 ID
        String email = testUser.getEmail();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(eventCouponRepository.findById(couponId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EventCouponNotFoundException.class, () -> {
            couponService.issueEventCoupon(couponId, email);
        });

        verify(userRepository, times(1)).findByEmail(email);
        verify(eventCouponRepository, times(1)).findById(couponId);
        verify(userEventCouponRepository, never()).existsByUserAndEventCoupon(any(), any());
        verify(statusManager, never()).getStatus(anyString(), anyString());
        verify(userEventCouponRepository, never()).save(any(UserEventCoupon.class));
    }

    @Test
    @DisplayName("이벤트 쿠폰 발급 실패_UserEventCouponAlreadyExists")
    void issueEventCoupon_UserEventCouponAlreadyExists() {
        // Given
        Long couponId = testEventCoupon.getEventCouponId();
        String email = testUser.getEmail();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(eventCouponRepository.findById(couponId)).thenReturn(Optional.of(testEventCoupon));
        when(userEventCouponRepository.existsByUserAndEventCoupon(testUser, testEventCoupon)).thenReturn(true); // 이미 존재함

        // When & Then
        assertThrows(UserEventCouponAlreadyExistsException.class, () -> {
            couponService.issueEventCoupon(couponId, email);
        });

        verify(userRepository, times(1)).findByEmail(email);
        verify(eventCouponRepository, times(1)).findById(couponId);
        verify(userEventCouponRepository, times(1)).existsByUserAndEventCoupon(testUser, testEventCoupon);
        verify(statusManager, never()).getStatus(anyString(), anyString());
        verify(userEventCouponRepository, never()).save(any(UserEventCoupon.class));
    }
}
