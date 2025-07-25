package eureca.capstone.project.orchestrator.pay.service.impl;

import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.common.exception.code.ErrorCode;
import eureca.capstone.project.orchestrator.common.exception.custom.InternalServerException;
import eureca.capstone.project.orchestrator.common.util.PayTypeManager;
import eureca.capstone.project.orchestrator.common.util.StatusManager;
import eureca.capstone.project.orchestrator.pay.entity.ChargeHistory;
import eureca.capstone.project.orchestrator.pay.entity.PayType;
import eureca.capstone.project.orchestrator.pay.repository.ChargeHistoryRepository;
import eureca.capstone.project.orchestrator.pay.service.PaymentTransactionService;
import eureca.capstone.project.orchestrator.pay.service.WebhookService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookServiceImpl implements WebhookService {
    private final ChargeHistoryRepository chargeHistoryRepository;
    private final PaymentTransactionService paymentTransactionService;
    private final PayTypeManager payTypeManager;
    private final StatusManager statusManager;

    @Override
    @Transactional
    public void processPaymentStatusChanged(Map<String, Object> payload) {
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) payload.get("data");
        String orderId = (String) data.get("orderId");
        String status = (String) data.get("status");
        log.info("[Webhook] 주문 상태 변경: orderId={}, status={}", orderId, status);

        ChargeHistory chargeHistory = chargeHistoryRepository.findByOrderIdWithDetails(orderId)
            .orElseThrow(() -> new InternalServerException(ErrorCode.ORDER_NOT_FOUND));

        Status requestedStatus = statusManager.getStatus("PAYMENT", "REQUESTED");
        if (!requestedStatus.equals(chargeHistory.getStatus())) {
            log.warn("[Webhook] 이미 처리된 주문입니다. orderId={}, currentStatus={}", orderId, chargeHistory.getStatus());
            return;
        }

        Status doneStatus = statusManager.getStatus("TOSS", "DONE");
        if (doneStatus.getCode().equals(status)) {
            String paymentKey = (String) data.get("paymentKey");
            PayType payType = getActualPaymentMethod(data);

            paymentTransactionService.processPaymentSuccess(chargeHistory.getChargeHistoryId(), paymentKey, payType);
            log.info("[Webhook] 결제 성공 처리 완료: orderId={}, paymentKey={}, payType={}", orderId, paymentKey, payType);

        } else {
            paymentTransactionService.processPaymentFailed(chargeHistory.getChargeHistoryId());
            log.info("[Webhook] 결제 실패/만료 처리 완료: orderId={}", orderId);
        }
    }

    private PayType getActualPaymentMethod(Map<String, Object> responseMap) {
        String method = (String) responseMap.get("method");
        if ("간편결제".equals(method)) {
            Map<String, Object> easyPay = (Map<String, Object>) responseMap.get("easyPay");
            method = (String) easyPay.get("provider");
        }
        return payTypeManager.getPayType(method);
    }
}
