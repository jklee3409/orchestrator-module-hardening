package eureca.capstone.project.orchestrator.transaction_feed.repository.custom;

import eureca.capstone.project.orchestrator.transaction_feed.dto.enums.WishListFilter;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionFeedRepositoryCustom {
    Optional<TransactionFeed> findFeedDetailById(Long transactionFeedId);
    Optional<TransactionFeed> findByIdWithLock(Long transactionFeedId);
    Page<TransactionFeed> findWishedFeeds(List<Long> feedIds, WishListFilter filter, Pageable pageable);
}
