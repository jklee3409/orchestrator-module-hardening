package eureca.capstone.project.orchestrator.transaction_feed.repository.custom;

import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.user.entity.User;
import java.util.List;

public interface LikedRepositoryCustom {
    boolean existsByFeedAndUser(TransactionFeed transactionFeed, User user);
    void removeByFeedAndUser(TransactionFeed transactionFeed, User user);
    List<Long> findLikedFeedIdsByUserAndFeedIds(User user, List<Long> transactionFeedIds);
}
