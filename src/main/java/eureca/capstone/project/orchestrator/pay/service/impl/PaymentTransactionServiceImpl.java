package eureca.capstone.project.orchestrator.pay.service.impl;

import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.common.exception.code.ErrorCode;
import eureca.capstone.project.orchestrator.common.exception.custom.InternalServerException;
import eureca.capstone.project.orchestrator.common.util.StatusManager;
import eureca.capstone.project.orchestrator.pay.entity.ChargeHistory;
import eureca.capstone.project.orchestrator.pay.entity.UserPay;
import eureca.capstone.project.orchestrator.pay.repository.ChargeHistoryRepository;
import eureca.capstone.project.orchestrator.pay.service.PayHistoryService;
import eureca.capstone.project.orchestrator.pay.service.PaymentTransactionService;
import eureca.capstone.project.orchestrator.pay.service.UserEventCouponService;
import eureca.capstone.project.orchestrator.pay.service.UserPayService;
import eureca.capstone.project.orchestrator.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentTransactionServiceImpl implements PaymentTransactionService {
    private final ChargeHistoryRepository chargeHistoryRepository;
    private final UserEventCouponService userEventCouponService;
    private final UserPayService userPayService;
    private final PayHistoryService payHistoryService;
    private final UserService userService;
    private final StatusManager statusManager;

    @Transactional
    public void processPaymentSuccess(Long chargeHistoryId, String paymentKey) {
        ChargeHistory history = findById(chargeHistoryId);
        log.info("[processPaymentSuccess] 충전 내역 결제 요청 성공으로 처리 시작. 충전 내역 ID: {}", chargeHistoryId);

        Status completedStatus = statusManager.getStatus("PAYMENT", "COMPLETED");
        history.processSuccess(paymentKey, completedStatus);
        log.info("[processPaymentSuccess] 충전 내역 결제 상태 완료로 처리. 충전 내역 ID: {}", chargeHistoryId);

        if (history.getUserEventCoupon() != null){
            userEventCouponService.useCoupon(history.getUserEventCoupon());
            log.info("[processPaymentSuccess] 사용자 이벤트 쿠폰 사용 처리 완료. 쿠폰 ID: {}", history.getUserEventCoupon().getUserEventCouponId());
        }

        UserPay updatedUserPay = userPayService.charge(history.getUser(), history.getChargePay());
        payHistoryService.createChargePayHistory(
                history.getUser(),
                history.getChargePay(),
                updatedUserPay.getPay(),
                history
        );
        log.info("[processPaymentSuccess] 페이 변동 내역 기록 완료. 충전 내역 ID: {}", chargeHistoryId);
    }

    @Transactional
    public void processPaymentFailed(Long chargeHistoryId) {
        ChargeHistory history = findById(chargeHistoryId);
        log.info("[processPaymentFailed] 충전 내역 결제 요청 실패 처리 시작. 충전 내역 ID: {}", chargeHistoryId);

        Status failedStatus = statusManager.getStatus("PAYMENT", "FAILED");
        history.processFailure(failedStatus);
        log.info("[processPaymentFailed] 충전 내역 상태 실패 처리. 충전 내역 ID: {}", chargeHistoryId);

        if(history.getUserEventCoupon() != null) {
            userEventCouponService.revertCoupon(history.getUserEventCoupon());
            log.info("[processPaymentFailed] 사용자 이벤트 쿠폰 상태를 다시 사용 가능하도록 변경. 쿠폰 ID: {}", history.getUserEventCoupon().getUserEventCouponId());
        }

        log.info("[processPaymentFailed] 충전 내역 결제 요청 실패 처리 완료. 충전 내역 ID: {}", chargeHistoryId);
    }

    private ChargeHistory findById(Long chargeHistoryId) {
        return chargeHistoryRepository.findById(chargeHistoryId)
                .orElseThrow(() -> new InternalServerException(ErrorCode.ORDER_NOT_FOUND));
    }
}
