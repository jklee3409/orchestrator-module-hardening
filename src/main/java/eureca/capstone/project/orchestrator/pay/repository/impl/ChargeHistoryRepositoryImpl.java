package eureca.capstone.project.orchestrator.pay.repository.impl;

import static eureca.capstone.project.orchestrator.common.entity.QStatus.status;
import static eureca.capstone.project.orchestrator.pay.entity.QChargeHistory.chargeHistory;
import static eureca.capstone.project.orchestrator.pay.entity.QUserEventCoupon.userEventCoupon;
import static eureca.capstone.project.orchestrator.user.entity.QUser.user;

import com.querydsl.jpa.impl.JPAQueryFactory;
import eureca.capstone.project.orchestrator.pay.entity.ChargeHistory;
import eureca.capstone.project.orchestrator.pay.repository.custom.ChargeHistoryRepositoryCustom;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChargeHistoryRepositoryImpl implements ChargeHistoryRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<ChargeHistory> findByOrderIdWithDetails(String orderId) {
        ChargeHistory result = queryFactory
                .selectFrom(chargeHistory)
                .join(chargeHistory.status, status).fetchJoin()
                .leftJoin(chargeHistory.user, user).fetchJoin()
                .leftJoin(chargeHistory.userEventCoupon, userEventCoupon).fetchJoin()
                .where(chargeHistory.orderId.eq(orderId))
                .fetchOne();

        return Optional.ofNullable(result);
    }
}
