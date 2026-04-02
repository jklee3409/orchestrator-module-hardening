package eureca.capstone.project.orchestrator.transaction_feed.repository;

import eureca.capstone.project.orchestrator.transaction_feed.entity.Bids;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.transaction_feed.repository.custom.BidsRepositoryCustom;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BidsRepository extends JpaRepository<Bids, Long>, BidsRepositoryCustom {
    List<Bids> findBidsWithUserByTransactionFeed(TransactionFeed transactionFeed);
    Optional<Bids> findTopByTransactionFeedOrderByBidAmountDescBidTimeDesc(TransactionFeed transactionFeed);
}
