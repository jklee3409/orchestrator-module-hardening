package eureca.capstone.project.orchestrator.pay.repository.custom;

import eureca.capstone.project.orchestrator.pay.entity.ChargeHistory;
import eureca.capstone.project.orchestrator.pay.entity.ExchangeHistory;
import eureca.capstone.project.orchestrator.pay.entity.PayHistory;
import eureca.capstone.project.orchestrator.transaction_feed.entity.DataTransactionHistory;
import eureca.capstone.project.orchestrator.user.entity.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PayHistoryRepositoryCustom {
    Page<PayHistory> findPayHistories(User user, Pageable pageable);
    Optional<ChargeHistory> findChargeHistoryByPayHistoryId(Long payHistoryId);
    Optional<ExchangeHistory> findExchangeHistoryByPayHistoryId(Long payHistoryId);
    Optional<DataTransactionHistory> findTransactionHistoryByPayHistoryId(Long payHistoryId);
}
