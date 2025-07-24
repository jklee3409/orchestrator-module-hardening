package eureca.capstone.project.orchestrator.pay.service.impl;

import eureca.capstone.project.orchestrator.common.exception.custom.InternalServerException;
import eureca.capstone.project.orchestrator.pay.dto.request.ExchangeRequestDto;
import eureca.capstone.project.orchestrator.pay.entity.Bank;
import eureca.capstone.project.orchestrator.pay.entity.ExchangeHistory;
import eureca.capstone.project.orchestrator.pay.entity.UserPay;
import eureca.capstone.project.orchestrator.pay.repository.BankRepository;
import eureca.capstone.project.orchestrator.pay.repository.ExchangeHistoryRepository;
import eureca.capstone.project.orchestrator.pay.repository.UserPayRepository;
import eureca.capstone.project.orchestrator.pay.service.PayHistoryService;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeServiceImplTest {

    @InjectMocks
    private ExchangeServiceImpl exchangeService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserPayRepository userPayRepository;
    @Mock
    private BankRepository bankRepository;
    @Mock
    private ExchangeHistoryRepository exchangeHistoryRepository;
    @Mock
    private PayHistoryService payHistoryService;

    private User user;
    private Bank bank;
    private ExchangeRequestDto requestDto;

    @BeforeEach
    void setUp() {
        user = User.builder().userId(1L).email("test@example.com").build();
        bank = Bank.builder().bankId(1L).bankName("테스트은행").build();

        requestDto = new ExchangeRequestDto();
        requestDto.setBankId(1L);
        requestDto.setExchangeAccount("123-456-789");
        requestDto.setAmount(10000L);
    }

    @Nested
    @DisplayName("페이 환전 기능")
    class ExchangePay {

        @Test
        @DisplayName("성공적으로 페이 환전 요청 처리")
        void exchangePay_Success() {
            // given
            UserPay userPay = new UserPay(user);
            userPay.charge(50000L);

            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(bankRepository.findById(requestDto.getBankId())).thenReturn(Optional.of(bank));
            when(userPayRepository.findById(user.getUserId())).thenReturn(Optional.of(userPay));

            // when
            exchangeService.exchangePay(user.getEmail(), requestDto);

            // then
            verify(userPayRepository).save(any(UserPay.class));
            verify(exchangeHistoryRepository).save(any(ExchangeHistory.class));
            verify(payHistoryService).createExchangePayHistory(any(User.class), any(Long.class), any(ExchangeHistory.class));
        }

        @Test
        @DisplayName("페이 잔액이 부족하면 예외 발생")
        void exchangePay_Fail_InsufficientBalance() {
            // given
            UserPay userPay = new UserPay(user);
            userPay.charge(5000L);

            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(bankRepository.findById(requestDto.getBankId())).thenReturn(Optional.of(bank));
            when(userPayRepository.findById(user.getUserId())).thenReturn(Optional.of(userPay));

            // when & then
            assertThrows(InternalServerException.class,
                    () -> exchangeService.exchangePay(user.getEmail(), requestDto));
        }
    }
}