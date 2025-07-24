package eureca.capstone.project.orchestrator.transaction_feed.repository.impl;

import static eureca.capstone.project.orchestrator.common.entity.QStatus.status;
import static eureca.capstone.project.orchestrator.common.entity.QTelecomCompany.telecomCompany;
import static eureca.capstone.project.orchestrator.transaction_feed.entity.QSalesType.salesType;
import static eureca.capstone.project.orchestrator.transaction_feed.entity.QTransactionFeed.transactionFeed;
import static eureca.capstone.project.orchestrator.user.entity.QUser.user;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import eureca.capstone.project.orchestrator.common.util.SalesTypeManager;
import eureca.capstone.project.orchestrator.common.util.StatusManager;
import eureca.capstone.project.orchestrator.transaction_feed.dto.enums.StatusFilter;
import eureca.capstone.project.orchestrator.transaction_feed.dto.enums.SalesTypeFilter;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.transaction_feed.repository.custom.TransactionFeedRepositoryCustom;
import jakarta.persistence.LockModeType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TransactionFeedRepositoryImpl implements TransactionFeedRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;
    private final SalesTypeManager salesTypeManager;
    private final StatusManager statusManager;

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
    public Page<TransactionFeed> findWishedFeeds(List<Long> feedIds, SalesTypeFilter filter, Pageable pageable) {
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

    @Override
    public Page<TransactionFeed> findMyFeeds(Long userId, SalesTypeFilter filter, StatusFilter feedStatus, Pageable pageable) {
        List<TransactionFeed> content = jpaQueryFactory
                .selectFrom(transactionFeed)
                .leftJoin(transactionFeed.user, user).fetchJoin()
                .leftJoin(transactionFeed.salesType, salesType).fetchJoin()
                .leftJoin(transactionFeed.telecomCompany, telecomCompany).fetchJoin()
                .leftJoin(transactionFeed.status, status).fetchJoin()
                .where(
                        user.userId.eq(userId),
                        transactionFeed.isDeleted.isFalse(),
                        salesTypeFilter(filter),
                        statusFilter(feedStatus)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifiers(pageable.getSort()))
                .fetch();

        Long count = jpaQueryFactory
                .select(transactionFeed.count())
                .from(transactionFeed)
                .leftJoin(transactionFeed.user, user)
                .where(
                        transactionFeed.user.userId.eq(userId),
                        transactionFeed.isDeleted.isFalse(),
                        salesTypeFilter(filter),
                        statusFilter(feedStatus)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, count == null ? 0 : count);
    }

    private BooleanExpression salesTypeFilter(SalesTypeFilter filter) {
        if (filter == SalesTypeFilter.NORMAL) {
            return transactionFeed.salesType.eq(salesTypeManager.getNormalSaleType());
        }
        if (filter == SalesTypeFilter.BID) {
            return transactionFeed.salesType.eq(salesTypeManager.getBidSaleType());
        }
        return null;
    }
    private BooleanExpression statusFilter(StatusFilter feedStatus) {
        if (feedStatus == StatusFilter.ON_SALE) {
            return transactionFeed.status.eq(statusManager.getStatus("FEED", "ON_SALE"));
        }
        if (feedStatus == StatusFilter.COMPLETED) {
            return transactionFeed.status.eq(statusManager.getStatus("FEED", "COMPLETED"));
        }
        if (feedStatus == StatusFilter.EXPIRED) {
            return transactionFeed.status.eq(statusManager.getStatus("FEED", "EXPIRED"));
        }
        return null;
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(Sort sort) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();
        sort.forEach(order -> {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            String prop = order.getProperty();
            switch (prop) {
                case "salesPrice":
                    orders.add(new OrderSpecifier<>(direction, transactionFeed.salesPrice));
                    break;
                case "createdAt":
                default:
                    orders.add(new OrderSpecifier<>(direction, transactionFeed.createdAt));
                    break;
            }
        });
        if (orders.isEmpty()) {
            orders.add(new OrderSpecifier<>(Order.DESC, transactionFeed.createdAt));
        }
        return orders.toArray(new OrderSpecifier[0]);
    }
}
