package eureca.capstone.project.orchestrator.pay.service.impl;

import eureca.capstone.project.orchestrator.common.exception.custom.PayLackException;
import eureca.capstone.project.orchestrator.common.exception.custom.UserNotFoundException;
import eureca.capstone.project.orchestrator.common.exception.custom.UserPayNotFoundException;
import eureca.capstone.project.orchestrator.pay.dto.response.GetPayBalanceResponseDto;
import eureca.capstone.project.orchestrator.pay.entity.UserPay;
import eureca.capstone.project.orchestrator.pay.repository.UserPayRepository;
import eureca.capstone.project.orchestrator.pay.service.UserPayService;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPayServiceImpl implements UserPayService {
    private final UserPayRepository userPayRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserPay charge(User user, Long amount) {
        log.info("[charge] 사용자 ID: 페이 충전 시작. 충전 금액: {}", user.getUserId());

        int updated = userPayRepository.increasePay(user.getUserId(), amount);
        if (updated == 0) {
            createUserPayIfAbsent(user, amount);
        }

        UserPay currentUserPay = getPersistedUserPay(user.getUserId());
        log.info("[charge] 사용자 ID: {} 페이 충전 완료. 현재 페이: {}", user.getUserId(), currentUserPay.getPay());
        return currentUserPay;
    }

    @Override
    @Transactional
    public void usePay(User user, Long amount) {
        log.info("[usePay] 사용자 ID: {} 페이 사용 시작. 사용 금액: {}", user.getUserId(), amount);

        int updated = userPayRepository.decreasePayIfEnough(user.getUserId(), amount);
        if (updated == 0) {
            log.error("[usePay] 사용자 ID: {} 페이 사용 실패. 잔액이 부족하거나 user_pay 행이 없습니다. 사용 금액: {}",
                    user.getUserId(), amount);
            throw new PayLackException();
        }

        log.info("[usePay] 사용자 ID: {} 페이 사용 완료. 사용 금액: {}", user.getUserId(), amount);
    }

    @Override
    @Transactional
    public void refundPay(User user, Long amount) {
        log.info("[refundPay] 사용자 ID: {} 페이 환불(지급) 시작. 환불 금액: {}", user.getUserId(), amount);
        charge(user, amount);
        UserPay userPay = findOrNewUserPay(user);
        log.info("[refundPay] 사용자 ID: {} 페이 환불(지급) 완료. 현재 페이: {}", user.getUserId(), userPay.getPay());
    }

    @Override
    @Transactional(readOnly = true)
    public GetPayBalanceResponseDto getPay(String email) {
        User user = findUserByEmail(email);
        log.info("[getPay] 사용자 ID: {} 페이 잔액 조회 시작.", user.getUserId());
        UserPay userPay = findOrNewUserPay(user);
        log.info("[getPay] 사용자 ID: {} 페이 잔액 조회 완료. 현재 페이: {}", user.getUserId(), userPay.getPay());
        return GetPayBalanceResponseDto.from(userPay.getPay());
    }

    private void createUserPayIfAbsent(User user, Long amount) {
        try {
            userPayRepository.saveAndFlush(
                    UserPay.builder()
                            .userId(user.getUserId())
                            .user(user)
                            .pay(amount)
                            .build()
            );
        } catch (DataIntegrityViolationException e) {
            log.info("[createUserPayIfAbsent] 사용자 ID: {} user_pay 행이 이미 생성되어 증가 연산으로 재시도합니다.",
                    user.getUserId());

            int updated = userPayRepository.increasePay(user.getUserId(), amount);
            if (updated == 0) {
                throw e;
            }
        }
    }

    private UserPay getPersistedUserPay(Long userId) {
        return userPayRepository.findById(userId)
                .orElseThrow(UserPayNotFoundException::new);
    }

    private UserPay findOrNewUserPay(User user) {
        return userPayRepository.findById(user.getUserId())
                .orElseGet(() -> new UserPay(user));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }
}
