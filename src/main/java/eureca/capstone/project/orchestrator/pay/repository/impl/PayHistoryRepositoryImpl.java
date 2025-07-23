package eureca.capstone.project.orchestrator.pay.repository.impl;

import static eureca.capstone.project.orchestrator.pay.entity.QChargeHistory.chargeHistory;
import static eureca.capstone.project.orchestrator.pay.entity.QChargeHistoryDetail.chargeHistoryDetail;
import static eureca.capstone.project.orchestrator.pay.entity.QExchangeHistory.exchangeHistory;
import static eureca.capstone.project.orchestrator.pay.entity.QExchangeHistoryDetail.exchangeHistoryDetail;
import static eureca.capstone.project.orchestrator.pay.entity.QPayHistory.payHistory;
import static eureca.capstone.project.orchestrator.pay.entity.QPayHistoryDetail.payHistoryDetail;
import static eureca.capstone.project.orchestrator.transaction_feed.entity.QDataTransactionHistory.dataTransactionHistory;

import com.querydsl.jpa.impl.JPAQueryFactory;
import eureca.capstone.project.orchestrator.pay.entity.ChargeHistory;
import eureca.capstone.project.orchestrator.pay.entity.ExchangeHistory;
import eureca.capstone.project.orchestrator.pay.entity.PayHistory;
import eureca.capstone.project.orchestrator.pay.repository.custom.PayHistoryRepositoryCustom;
import eureca.capstone.project.orchestrator.transaction_feed.entity.DataTransactionHistory;
import eureca.capstone.project.orchestrator.user.entity.User;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PayHistoryRepositoryImpl implements PayHistoryRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<PayHistory> findPayHistories(User user, Pageable pageable) {
        List<PayHistory> content = queryFactory
                .selectFrom(payHistory)
                .join(payHistory.changeType).fetchJoin()
                .where(payHistory.user.eq(user))
                .orderBy(payHistory.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .selectFrom(payHistory)
                .where(payHistory.user.eq(user))
                .fetchCount();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Optional<ChargeHistory> findChargeHistoryByPayHistoryId(Long payHistoryId) {
        return Optional.ofNullable(queryFactory
                .select(chargeHistory)
                .from(chargeHistoryDetail)
                .join(chargeHistoryDetail.chargeHistory, chargeHistory)
                .where(chargeHistoryDetail.payHistory.payHistoryId.eq(payHistoryId))
                .fetchOne());
    }

    @Override
    public Optional<ExchangeHistory> findExchangeHistoryByPayHistoryId(Long payHistoryId) {
        return Optional.ofNullable(queryFactory
                .select(exchangeHistory)
                .from(exchangeHistoryDetail)
                .join(exchangeHistoryDetail.exchangeHistory, exchangeHistory)
                .where(exchangeHistoryDetail.payHistory.payHistoryId.eq(payHistoryId))
                .fetchOne());
    }

    @Override
    public Optional<DataTransactionHistory> findTransactionHistoryByPayHistoryId(Long payHistoryId) {
        return Optional.ofNullable(queryFactory
                .select(dataTransactionHistory)
                .from(payHistoryDetail)
                .join(payHistoryDetail.dataTransactionHistory, dataTransactionHistory)
                .join(dataTransactionHistory.transactionFeed).fetchJoin()
                .where(payHistoryDetail.payHistory.payHistoryId.eq(payHistoryId))
                .fetchOne());
    }
}
