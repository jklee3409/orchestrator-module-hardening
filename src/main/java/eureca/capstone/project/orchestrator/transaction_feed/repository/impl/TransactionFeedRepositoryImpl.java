package eureca.capstone.project.orchestrator.transaction_feed.repository.impl;

import static eureca.capstone.project.orchestrator.common.entity.QStatus.status;
import static eureca.capstone.project.orchestrator.common.entity.QTelecomCompany.telecomCompany;
import static eureca.capstone.project.orchestrator.transaction_feed.entity.QSalesType.salesType;
import static eureca.capstone.project.orchestrator.transaction_feed.entity.QTransactionFeed.transactionFeed;
import static eureca.capstone.project.orchestrator.user.entity.QUser.user;

import com.querydsl.jpa.impl.JPAQueryFactory;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.transaction_feed.repository.custom.TransactionFeedRepositoryCustom;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TransactionFeedRepositoryImpl implements TransactionFeedRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<TransactionFeed> findFeedDetailById(Long transactionFeedId) {
        TransactionFeed result = jpaQueryFactory
                .selectFrom(transactionFeed)
                .leftJoin(transactionFeed.user, user).fetchJoin()
                .leftJoin(transactionFeed.salesType, salesType).fetchJoin()
                .leftJoin(transactionFeed.telecomCompany, telecomCompany).fetchJoin()
                .leftJoin(transactionFeed.status, status).fetchJoin()
                .where(transactionFeed.transactionFeedId.eq(transactionFeedId)
                        .and(transactionFeed.isDeleted.eq(false)))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Optional<TransactionFeed> findByIdWithLock(Long transactionFeedId) {
        TransactionFeed result = jpaQueryFactory
                .selectFrom(transactionFeed)
                .where(transactionFeed.transactionFeedId.eq(transactionFeedId))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetchOne();

        return Optional.ofNullable(result);
    }
}
