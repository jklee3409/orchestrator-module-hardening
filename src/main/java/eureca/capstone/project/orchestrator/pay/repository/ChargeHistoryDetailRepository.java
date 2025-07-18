package eureca.capstone.project.orchestrator.pay.repository;

import eureca.capstone.project.orchestrator.pay.entity.ChargeHistoryDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChargeHistoryDetailRepository extends JpaRepository<ChargeHistoryDetail, Long> {
}
