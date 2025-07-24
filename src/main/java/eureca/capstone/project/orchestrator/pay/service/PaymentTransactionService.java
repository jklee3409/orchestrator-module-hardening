package eureca.capstone.project.orchestrator.pay.service;

import eureca.capstone.project.orchestrator.pay.entity.PayType;

public interface PaymentTransactionService {
    void processPaymentSuccess(Long chargeHistoryId, String paymentKey, PayType payType);
    void processPaymentFailed(Long chargeHistoryId);
}
