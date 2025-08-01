package eureca.capstone.project.orchestrator.transaction_feed.repository.custom;

import eureca.capstone.project.orchestrator.transaction_feed.entity.Bids;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import java.util.List;
import java.util.Optional;

public interface BidsRepositoryCustom {
    List<Bids> findBidsWithUserByTransactionFeed(TransactionFeed transactionFeed);
    Optional<Bids> findHighestBidByFeed(TransactionFeed feed);
}
