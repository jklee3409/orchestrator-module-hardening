package eureca.capstone.project.orchestrator.transaction_feed.repository.impl;

import static eureca.capstone.project.orchestrator.transaction_feed.entity.QBids.bids;
import static eureca.capstone.project.orchestrator.user.entity.QUser.user;

import com.querydsl.jpa.impl.JPAQueryFactory;
import eureca.capstone.project.orchestrator.transaction_feed.entity.Bids;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.transaction_feed.repository.custom.BidsRepositoryCustom;
import java.util.List;
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
                .orderBy(bids.createdAt.desc())
                .fetch();
    }
}
