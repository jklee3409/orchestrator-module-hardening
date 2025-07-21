package eureca.capstone.project.orchestrator.transaction_feed.repository.custom;

import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import java.util.Optional;

public interface TransactionFeedRepositoryCustom {
    Optional<TransactionFeed> findFeedDetailById(Long transactionFeedId);

    Optional<TransactionFeed> findByIdWithLock(Long transactionFeedId);
}
