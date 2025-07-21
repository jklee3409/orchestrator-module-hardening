package eureca.capstone.project.orchestrator.transaction_feed.repository.custom;

import eureca.capstone.project.orchestrator.transaction_feed.entity.Bids;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import java.util.List;

public interface BidsRepositoryCustom {
    List<Bids> findBidsWithUserByTransactionFeed(TransactionFeed transactionFeed);
}
