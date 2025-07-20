package eureca.capstone.project.orchestrator.transaction_feed.repository;

import eureca.capstone.project.orchestrator.transaction_feed.entity.DataTransactionHistory;
import eureca.capstone.project.orchestrator.transaction_feed.repository.custom.DataTransactionHistoryRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataTransactionHistoryRepository extends JpaRepository<DataTransactionHistory, Long>,
        DataTransactionHistoryRepositoryCustom {
}
