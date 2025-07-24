package eureca.capstone.project.orchestrator.pay.repository;

import eureca.capstone.project.orchestrator.pay.entity.ExchangeHistoryDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeHistoryDetailRepository extends JpaRepository<ExchangeHistoryDetail, Long> {
}
