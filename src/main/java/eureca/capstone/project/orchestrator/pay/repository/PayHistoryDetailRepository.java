package eureca.capstone.project.orchestrator.pay.repository;

import eureca.capstone.project.orchestrator.pay.entity.PayHistoryDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayHistoryDetailRepository extends JpaRepository<PayHistoryDetail, Long> {
}
