package eureca.capstone.project.orchestrator.transaction_feed.repository.impl;

import static eureca.capstone.project.orchestrator.transaction_feed.entity.QTransactionFeed.transactionFeed;

import com.querydsl.jpa.impl.JPAQueryFactory;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.transaction_feed.repository.custom.TransactionFeedRepositoryCustom;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TransactionFeedRepositoryImpl implements TransactionFeedRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<TransactionFeed> findByIdWithLock(Long transactionFeedId) {
        TransactionFeed result = jpaQueryFactory
                .selectFrom(transactionFeed)
                .where(transactionFeed.transactionFeedId.eq(transactionFeedId))
                .fetchOne();

        return Optional.ofNullable(result);
    }
}
