package eureca.capstone.project.orchestrator.pay.service.impl;

import eureca.capstone.project.orchestrator.common.exception.code.ErrorCode;
import eureca.capstone.project.orchestrator.common.exception.custom.InternalServerException;
import eureca.capstone.project.orchestrator.common.exception.custom.UserNotFoundException;
import eureca.capstone.project.orchestrator.pay.dto.BankDto;
import eureca.capstone.project.orchestrator.pay.dto.request.ExchangeRequestDto;
import eureca.capstone.project.orchestrator.pay.entity.Bank;
import eureca.capstone.project.orchestrator.pay.entity.ExchangeHistory;
import eureca.capstone.project.orchestrator.pay.entity.UserPay;
import eureca.capstone.project.orchestrator.pay.repository.BankRepository;
import eureca.capstone.project.orchestrator.pay.repository.ExchangeHistoryRepository;
import eureca.capstone.project.orchestrator.pay.repository.UserPayRepository;
import eureca.capstone.project.orchestrator.pay.service.ExchangeService;
import eureca.capstone.project.orchestrator.pay.service.PayHistoryService;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeServiceImpl implements ExchangeService {
    private static final double FEE_RATE = 0.03;

    private final UserRepository userRepository;
    private final UserPayRepository userPayRepository;
    private final BankRepository bankRepository;
    private final ExchangeHistoryRepository exchangeHistoryRepository;
    private final PayHistoryService payHistoryService;

    @Override
    @Transactional
    public void exchangePay(String email, ExchangeRequestDto requestDto) {
        log.info("[exchangePay] 사용자 {}의 페이 환전 요청 시작", email);

        User user = findUserByEmail(email);
        Bank bank = bankRepository.findById(requestDto.getBankId())
                .orElseThrow(() -> new InternalServerException(ErrorCode.BANK_NOT_FOUND));
        log.info("[exchangePay] 사용자 페이 정보 조회 완료. 사용자 ID: {}, 은행 ID: {}", user.getUserId(), bank.getBankId());

        UserPay userPay = userPayRepository.findById(user.getUserId())
                .orElseGet(() -> new UserPay(user));

        Long fee = (long) (requestDto.getAmount() * FEE_RATE);
        Long finalAmount = requestDto.getAmount() - fee;

        if (userPay.getPay() < requestDto.getAmount()) {
            throw new InternalServerException(ErrorCode.USER_PAY_LACK);
        }

        userPay.use(requestDto.getAmount());
        userPayRepository.save(userPay);
        log.info("[exchangePay] 사용자 페이 차감 완료. 차감액: {}, 현재 잔액: {}", requestDto.getAmount(), userPay.getPay());

        ExchangeHistory exchangeHistory = ExchangeHistory.builder()
                .user(user)
                .bank(bank)
                .exchangeAccount(requestDto.getExchangeAccount())
                .amount(requestDto.getAmount())
                .fee(fee)
                .finalAmount(finalAmount)
                .build();
        exchangeHistoryRepository.save(exchangeHistory);
        log.info("[exchangePay] 환전 내역 저장 완료. ID: {}", exchangeHistory.getExchangeHistoryId());

        payHistoryService.createExchangePayHistory(user, -requestDto.getAmount(), exchangeHistory);
        log.info("[exchangePay] 페이 변동 내역 기록 완료.");
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankDto> getBankList() {
        log.info("[getBankList] 은행 목록 조회 시작");
        List<Bank> banks = bankRepository.findAll();
        log.info("[getBankList] 은행 목록 조회 완료. 은행 개수: {}", banks.size());

        return banks.stream()
                .map(BankDto::fromEntity)
                .toList();
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }
}
