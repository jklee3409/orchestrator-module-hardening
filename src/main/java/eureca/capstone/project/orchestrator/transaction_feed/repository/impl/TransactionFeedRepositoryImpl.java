package eureca.capstone.project.orchestrator.transaction_feed.repository.impl;

import static eureca.capstone.project.orchestrator.common.entity.QStatus.status;
import static eureca.capstone.project.orchestrator.common.entity.QTelecomCompany.telecomCompany;
import static eureca.capstone.project.orchestrator.transaction_feed.entity.QSalesType.salesType;
import static eureca.capstone.project.orchestrator.transaction_feed.entity.QTransactionFeed.transactionFeed;
import static eureca.capstone.project.orchestrator.user.entity.QUser.user;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import eureca.capstone.project.orchestrator.common.util.SalesTypeManager;
import eureca.capstone.project.orchestrator.transaction_feed.dto.enums.WishListFilter;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.transaction_feed.repository.custom.TransactionFeedRepositoryCustom;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TransactionFeedRepositoryImpl implements TransactionFeedRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;
    private final SalesTypeManager salesTypeManager;

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

    @Override
    public Page<TransactionFeed> findWishedFeeds(List<Long> feedIds, WishListFilter filter, Pageable pageable) {
        List<TransactionFeed> content = jpaQueryFactory
                .selectFrom(transactionFeed)
                .where(
                        transactionFeed.transactionFeedId.in(feedIds),
                        transactionFeed.isDeleted.isFalse(),
                        salesTypeFilter(filter)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(transactionFeed.createdAt.desc())
                .fetch();

        Long count = jpaQueryFactory
                .select(transactionFeed.count())
                .from(transactionFeed)
                .where(
                        transactionFeed.transactionFeedId.in(feedIds),
                        transactionFeed.isDeleted.isFalse(),
                        salesTypeFilter(filter)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, count == null ? 0 : count);
    }

    private BooleanExpression salesTypeFilter(WishListFilter filter) {
        if (filter == WishListFilter.NORMAL) {
            return transactionFeed.salesType.eq(salesTypeManager.getNormalSaleType());
        }
        if (filter == WishListFilter.BID) {
            return transactionFeed.salesType.eq(salesTypeManager.getBidSaleType());
        }
        return null;
    }
}
