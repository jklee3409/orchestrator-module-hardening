package eureca.capstone.project.orchestrator.transaction_feed.repository.impl;

import static eureca.capstone.project.orchestrator.transaction_feed.entity.QLiked.liked;

import com.querydsl.jpa.impl.JPAQueryFactory;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.transaction_feed.repository.custom.LikedRepositoryCustom;
import eureca.capstone.project.orchestrator.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LikedRepositoryImpl implements LikedRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public boolean existsByFeedAndUser(TransactionFeed transactionFeed, User user) {
        return jpaQueryFactory
                .selectOne()
                .from(liked)
                .where(
                        liked.user.eq(user),
                        liked.transactionFeed.eq(transactionFeed)
                )
                .fetchFirst() != null;
    }

    @Override
    public void removeByFeedAndUser(TransactionFeed transactionFeed, User user) {
        jpaQueryFactory
                .delete(liked)
                .where(
                        liked.user.eq(user),
                        liked.transactionFeed.eq(transactionFeed)
                )
                .execute();
    }
}
