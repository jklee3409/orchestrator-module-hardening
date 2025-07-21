package eureca.capstone.project.orchestrator.pay.service;

import eureca.capstone.project.orchestrator.pay.entity.ChargeHistory;
import eureca.capstone.project.orchestrator.transaction_feed.entity.DataTransactionHistory;
import eureca.capstone.project.orchestrator.user.entity.User;

public interface PayHistoryService {
    void createChargePayHistory(User user, Long changedPay, Long finalPay, ChargeHistory chargeHistory);
    void createPurchasePayHistory(User buyer, Long changedPay, DataTransactionHistory dataTransactionHistory);
    void createSalePayHistory(User seller, Long changedPay, DataTransactionHistory transactionHistory);
}
