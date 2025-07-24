package eureca.capstone.project.orchestrator.pay.service;

import eureca.capstone.project.orchestrator.pay.dto.PayHistoryDto;
import eureca.capstone.project.orchestrator.pay.dto.response.PayHistoryDetailResponseDto;
import eureca.capstone.project.orchestrator.pay.entity.ChargeHistory;
import eureca.capstone.project.orchestrator.pay.entity.ExchangeHistory;
import eureca.capstone.project.orchestrator.transaction_feed.entity.DataTransactionHistory;
import eureca.capstone.project.orchestrator.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PayHistoryService {
    Page<PayHistoryDto.PayHistorySimpleDto> getPayHistoryList(String email, Pageable pageable);
    PayHistoryDetailResponseDto getPayHistoryDetail(String email, Long payHistoryId);
    void createChargePayHistory(User user, Long changedPay, Long finalPay, ChargeHistory chargeHistory);
    void createPurchasePayHistory(User buyer, Long changedPay, DataTransactionHistory dataTransactionHistory);
    void createSalePayHistory(User seller, Long changedPay, DataTransactionHistory transactionHistory);
    void createExchangePayHistory(User user, Long changedPay, ExchangeHistory exchangeHistory);
}
