package eureca.capstone.project.orchestrator.transaction_feed.repository;

import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.transaction_feed.repository.custom.TransactionFeedRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionFeedRepository extends JpaRepository<TransactionFeed, Long>, TransactionFeedRepositoryCustom {
}
