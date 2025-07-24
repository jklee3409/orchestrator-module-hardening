package eureca.capstone.project.orchestrator.transaction_feed.repository.custom;

import eureca.capstone.project.orchestrator.transaction_feed.dto.enums.StatusFilter;
import eureca.capstone.project.orchestrator.transaction_feed.dto.enums.SalesTypeFilter;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionFeedRepositoryCustom {
    Optional<TransactionFeed> findFeedDetailById(Long transactionFeedId);
    Optional<TransactionFeed> findByIdWithLock(Long transactionFeedId);
    Page<TransactionFeed> findWishedFeeds(List<Long> feedIds, SalesTypeFilter filter, Pageable pageable);
    Page<TransactionFeed> findMyFeeds(Long userId, SalesTypeFilter filter, StatusFilter status, Pageable pageable);
}
