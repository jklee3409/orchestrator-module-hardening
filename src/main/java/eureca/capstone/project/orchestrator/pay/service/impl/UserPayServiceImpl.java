package eureca.capstone.project.orchestrator.pay.service.impl;

import eureca.capstone.project.orchestrator.pay.entity.UserPay;
import eureca.capstone.project.orchestrator.pay.repository.UserPayRepository;
import eureca.capstone.project.orchestrator.pay.service.UserPayService;
import eureca.capstone.project.orchestrator.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPayServiceImpl implements UserPayService {
    private final UserPayRepository userPayRepository;

    @Override
    @Transactional
    public UserPay charge(User user, Long amount) {
        log.info("[charge] 사용자 ID: 페이 충전 시작. 충전 금액: {}", user.getUserId());
        UserPay userPay = findOrNewUserPay(user);
        userPay.charge(amount);
        userPayRepository.save(userPay);
        log.info("[charge] 사용자 ID: {} 페이 충전 완료. 현재 페이: {}", user.getUserId(), userPay.getPay());
        return userPay;
    }

    @Override
    @Transactional
    public void usePay(User user, Long amount) {
        log.info("[usePay] 사용자 ID: {} 페이 사용 시작. 사용 금액: {}", user.getUserId(), amount);
        UserPay userPay = findOrNewUserPay(user);
        userPay.use(amount);
        userPayRepository.save(userPay);
    }

    @Override
    @Transactional
    public void refundPay(User user, Long amount) {
        log.info("[refundPay] 사용자 ID: {} 페이 환불(지급) 시작. 환불 금액: {}", user.getUserId(), amount);
        charge(user, amount);
        UserPay userPay = findOrNewUserPay(user);
        log.info("[refundPay] 사용자 ID: {} 페이 환불(지급) 완료. 현재 페이: {}", user.getUserId(), userPay.getPay());
    }

    private UserPay findOrNewUserPay(User user) {
        return userPayRepository.findById(user.getUserId())
                .orElseGet(() -> new UserPay(user));
    }
}
