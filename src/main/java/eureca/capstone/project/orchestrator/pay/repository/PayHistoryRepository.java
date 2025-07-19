package eureca.capstone.project.orchestrator.pay.repository;

import eureca.capstone.project.orchestrator.pay.entity.PayHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayHistoryRepository extends JpaRepository<PayHistory, Long> {
}
