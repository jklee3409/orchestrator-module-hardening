package eureca.capstone.project.orchestrator.transaction_feed.repository.impl;

import static eureca.capstone.project.orchestrator.transaction_feed.entity.QBids.bids;
import static eureca.capstone.project.orchestrator.user.entity.QUser.user;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import eureca.capstone.project.orchestrator.transaction_feed.entity.Bids;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.transaction_feed.repository.custom.BidsRepositoryCustom;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BidsRepositoryImpl implements BidsRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Bids> findBidsWithUserByTransactionFeed(TransactionFeed transactionFeed) {
        return queryFactory
                .selectFrom(bids)
                .join(bids.user, user).fetchJoin()
                .where(bids.transactionFeed.eq(transactionFeed))
                .orderBy(bids.bidTime.desc())
                .fetch();
    }

    @Override
    public Optional<Bids> findHighestBidByFeed(TransactionFeed feed) {
        Bids result = queryFactory
                .selectFrom(bids)
                .where(bids.transactionFeed.eq(feed))
                .orderBy(bids.bidAmount.desc())
                .limit(1)
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Map<Long, Long> findHighestBidAmountsByTransactionFeedIds(List<Long> transactionFeedIds) {
        if (transactionFeedIds == null || transactionFeedIds.isEmpty()) {
            return Collections.emptyMap();
        }

        NumberExpression<Long> maxBidAmount = bids.bidAmount.max();
        List<Tuple> tuples = queryFactory
                .select(bids.transactionFeed.transactionFeedId, maxBidAmount)
                .from(bids)
                .where(bids.transactionFeed.transactionFeedId.in(transactionFeedIds))
                .groupBy(bids.transactionFeed.transactionFeedId)
                .fetch();

        return tuples.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(bids.transactionFeed.transactionFeedId),
                        tuple -> tuple.get(maxBidAmount)
                ));
    }
}
