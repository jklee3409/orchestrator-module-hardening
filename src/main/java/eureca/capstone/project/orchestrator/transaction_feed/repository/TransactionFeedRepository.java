package eureca.capstone.project.orchestrator.transaction_feed.repository;

import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.transaction_feed.repository.custom.TransactionFeedRepositoryCustom;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface TransactionFeedRepository extends JpaRepository<TransactionFeed, Long>, TransactionFeedRepositoryCustom {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select tf
              from TransactionFeed tf
             where tf.transactionFeedId = :transactionFeedId
            """)
    Optional<TransactionFeed> findByIdForUpdate(@Param("transactionFeedId") Long transactionFeedId);
}
