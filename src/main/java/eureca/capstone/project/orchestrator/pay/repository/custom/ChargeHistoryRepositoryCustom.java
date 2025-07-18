package eureca.capstone.project.orchestrator.pay.repository.custom;

import eureca.capstone.project.orchestrator.pay.entity.ChargeHistory;
import java.util.Optional;

public interface ChargeHistoryRepositoryCustom {
    Optional<ChargeHistory> findByOrderIdWithDetails(String orderId);
}
