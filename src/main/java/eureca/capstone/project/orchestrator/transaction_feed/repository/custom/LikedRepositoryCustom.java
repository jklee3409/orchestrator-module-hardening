package eureca.capstone.project.orchestrator.transaction_feed.repository.custom;

import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.user.entity.User;

public interface LikedRepositoryCustom {
    boolean existsByFeedAndUser(TransactionFeed transactionFeed, User user);
    void removeByFeedAndUser(TransactionFeed transactionFeed, User user);
}
