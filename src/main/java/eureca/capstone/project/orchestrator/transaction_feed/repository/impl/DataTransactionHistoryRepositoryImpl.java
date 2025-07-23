package eureca.capstone.project.orchestrator.transaction_feed.repository.impl;

import static eureca.capstone.project.orchestrator.transaction_feed.entity.QDataTransactionHistory.dataTransactionHistory;
import static eureca.capstone.project.orchestrator.transaction_feed.entity.QTransactionFeed.transactionFeed;
import static eureca.capstone.project.orchestrator.user.entity.QUser.user;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import eureca.capstone.project.orchestrator.transaction_feed.dto.UserTransactionAverageDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.enums.TransactionHistoryType;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.GetTransactionHistoryResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.entity.DataTransactionHistory;
import eureca.capstone.project.orchestrator.transaction_feed.repository.custom.DataTransactionHistoryRepositoryCustom;
import eureca.capstone.project.orchestrator.user.entity.QUser;
import eureca.capstone.project.orchestrator.user.entity.User;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
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

    @Override
    public Page<DataTransactionHistory> findTransactionHistoryByUserId(Long userId, TransactionHistoryType type, Pageable pageable) {
        List<DataTransactionHistory> content = jpaQueryFactory
                .selectFrom(dataTransactionHistory)
                .join(dataTransactionHistory.transactionFeed, transactionFeed).fetchJoin()
                .join(dataTransactionHistory.user, user).fetchJoin()
                .join(transactionFeed.user).fetchJoin()
                .join(transactionFeed.telecomCompany).fetchJoin()
                .join(transactionFeed.salesType).fetchJoin()
                .where(createFilter(userId, type))
                .orderBy(dataTransactionHistory.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(dataTransactionHistory.count())
                .from(dataTransactionHistory)
                .join(dataTransactionHistory.transactionFeed, transactionFeed)
                .where(createFilter(userId, type));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression createFilter(Long userId, TransactionHistoryType type) {
        return switch (type) {
            case PURCHASE -> dataTransactionHistory.user.userId.eq(userId);
            case SALE -> transactionFeed.user.userId.eq(userId);
            default -> dataTransactionHistory.user.userId.eq(userId).or(transactionFeed.user.userId.eq(userId));
        };
    }
}
