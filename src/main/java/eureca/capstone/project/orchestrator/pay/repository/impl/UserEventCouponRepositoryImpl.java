package eureca.capstone.project.orchestrator.pay.repository.impl;

import static eureca.capstone.project.orchestrator.common.entity.QStatus.status;
import static eureca.capstone.project.orchestrator.pay.entity.QEventCoupon.eventCoupon;
import static eureca.capstone.project.orchestrator.pay.entity.QPayType.payType;
import static eureca.capstone.project.orchestrator.pay.entity.QUserEventCoupon.userEventCoupon;

import com.querydsl.jpa.impl.JPAQueryFactory;
import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.pay.entity.UserEventCoupon;
import eureca.capstone.project.orchestrator.pay.repository.custom.UserEventCouponRepositoryCustom;
import eureca.capstone.project.orchestrator.user.entity.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserEventCouponRepositoryImpl implements UserEventCouponRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<UserEventCoupon> findAvailableCouponsByUserAndStatus(User user, Status issuedStatus) {
        return queryFactory
                .selectFrom(userEventCoupon)
                .join(userEventCoupon.status, status).fetchJoin()
                .join(userEventCoupon.eventCoupon, eventCoupon).fetchJoin()
                .leftJoin(eventCoupon.payType, payType).fetchJoin()
                .where(
                        userEventCoupon.user.eq(user),
                        userEventCoupon.status.eq(issuedStatus)
                )
                .fetch();
    }
}
