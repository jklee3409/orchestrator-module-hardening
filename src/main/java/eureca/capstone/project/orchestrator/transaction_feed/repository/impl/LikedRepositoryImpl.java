package eureca.capstone.project.orchestrator.transaction_feed.repository.impl;

import static eureca.capstone.project.orchestrator.transaction_feed.entity.QLiked.liked;

import com.querydsl.jpa.impl.JPAQueryFactory;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.transaction_feed.repository.custom.LikedRepositoryCustom;
import eureca.capstone.project.orchestrator.user.entity.User;
import java.util.List;
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
    public void removeByUserAndFeedIds(List<Long> transactionFeedIds, User user) {
        jpaQueryFactory
                .delete(liked)
                .where(
                        liked.user.eq(user),
                        liked.transactionFeed.transactionFeedId.in(transactionFeedIds)
                )
                .execute();
    }

    @Override
    public List<Long> findLikedFeedIdsByUserAndFeedIds(User user, List<Long> transactionFeedIds) {
        return jpaQueryFactory
                .select(liked.transactionFeed.transactionFeedId)
                .from(liked)
                .where(
                        liked.user.eq(user),
                        liked.transactionFeed.transactionFeedId.in(transactionFeedIds)
                )
                .fetch();
    }

    @Override
    public List<Long> findFeedIdsByUser(User user) {
        return jpaQueryFactory
                .select(liked.transactionFeed.transactionFeedId)
                .from(liked)
                .where(liked.user.eq(user))
                .orderBy(liked.createdAt.desc())
                .fetch();
    }
}
