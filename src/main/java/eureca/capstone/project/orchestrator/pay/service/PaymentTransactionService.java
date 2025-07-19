package eureca.capstone.project.orchestrator.pay.service;

public interface PaymentTransactionService {
    void processPaymentSuccess(Long chargeHistoryId, String paymentKey);
    void processPaymentFailed(Long chargeHistoryId);
}
