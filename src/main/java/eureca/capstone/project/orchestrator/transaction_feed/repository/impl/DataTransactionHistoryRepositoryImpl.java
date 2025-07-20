package eureca.capstone.project.orchestrator.transaction_feed.repository.impl;

import static eureca.capstone.project.orchestrator.transaction_feed.entity.QDataTransactionHistory.dataTransactionHistory;
import static eureca.capstone.project.orchestrator.transaction_feed.entity.QTransactionFeed.transactionFeed;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import eureca.capstone.project.orchestrator.transaction_feed.dto.UserTransactionAverageDto;
import eureca.capstone.project.orchestrator.transaction_feed.repository.custom.DataTransactionHistoryRepositoryCustom;
import eureca.capstone.project.orchestrator.user.entity.User;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DataTransactionHistoryRepositoryImpl implements DataTransactionHistoryRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<UserTransactionAverageDto> findAverageByUser(User user) {
        UserTransactionAverageDto result = jpaQueryFactory
                .select(Projections.constructor(UserTransactionAverageDto.class,
                        dataTransactionHistory.transactionFinalPrice.avg(),
                        transactionFeed.salesDataAmount.avg()
                ))
                .from(dataTransactionHistory)
                .join(dataTransactionHistory.transactionFeed, transactionFeed)
                .where(dataTransactionHistory.user.eq(user))
                .fetchOne();

        return Optional.ofNullable(result)
                .filter(r -> r.getAveragePrice() != null && r.getAverageDataAmount() != null);
    }
}
