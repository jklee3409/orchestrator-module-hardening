package eureca.capstone.project.orchestrator.pay.repository;

import eureca.capstone.project.orchestrator.pay.entity.ExchangeHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeHistoryRepository extends JpaRepository<ExchangeHistory, Long> {
}
