package eureca.capstone.project.orchestrator.pay.service.impl;

import eureca.capstone.project.orchestrator.common.exception.custom.UserNotFoundException;
import eureca.capstone.project.orchestrator.pay.dto.response.GetPayBalanceResponseDto;
import eureca.capstone.project.orchestrator.pay.entity.UserPay;
import eureca.capstone.project.orchestrator.pay.repository.UserPayRepository;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPayServiceImplTest {

    @InjectMocks
    private UserPayServiceImpl userPayService;

    @Mock
    private UserPayRepository userPayRepository;

    @Mock
    private UserRepository userRepository;

    private User user;
    private String email;

    @BeforeEach
    void setUp() {
        email = "test@example.com";
        user = User.builder()
                .userId(1L)
                .email(email)
                .nickname("테스트유저")
                .build();
    }

    @Nested
    @DisplayName("페이 조회 기능")
    class GetUserPay {

        @Test
        @DisplayName("기존 페이 정보가 있는 경우 정확한 잔액을 반환해야 한다")
        void getPay_Success_WhenPayExists() {
            // given
            UserPay existingUserPay = new UserPay(user);
            existingUserPay.charge(5000L);

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
            when(userPayRepository.findById(user.getUserId())).thenReturn(Optional.of(existingUserPay));

            // when
            GetPayBalanceResponseDto result = userPayService.getPay(email);

            // then
            assertThat(result.getBalance()).isEqualTo(5000L);
            verify(userRepository).findByEmail(email);
            verify(userPayRepository).findById(user.getUserId());
        }

        @Test
        @DisplayName("기존 페이 정보가 없는 경우 잔액으로 0을 반환해야 한다")
        void getPay_Success_WhenPayNotExists() {
            // given
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
            when(userPayRepository.findById(user.getUserId())).thenReturn(Optional.empty());

            // when
            GetPayBalanceResponseDto result = userPayService.getPay(email);

            // then
            assertThat(result.getBalance()).isEqualTo(0L);
            verify(userRepository).findByEmail(email);
            verify(userPayRepository).findById(user.getUserId());
        }

        @Test
        @DisplayName("사용자를 찾을 수 없는 경우 예외가 발생해야 한다")
        void getPay_Fail_WhenUserNotFound() {
            // given
            String nonExistentEmail = "nonexistent@example.com";
            when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

            // when & then
            assertThrows(UserNotFoundException.class, () -> {
                userPayService.getPay(nonExistentEmail);
            });
            verify(userRepository).findByEmail(nonExistentEmail);
        }
    }
}