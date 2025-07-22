package eureca.capstone.project.orchestrator.transaction_feed.repository.impl;

import static eureca.capstone.project.orchestrator.common.entity.QStatus.status;
import static eureca.capstone.project.orchestrator.common.entity.QTelecomCompany.telecomCompany;
import static eureca.capstone.project.orchestrator.transaction_feed.entity.QDataCoupon.dataCoupon;
import static eureca.capstone.project.orchestrator.transaction_feed.entity.QUserDataCoupon.userDataCoupon;

import com.querydsl.jpa.impl.JPAQueryFactory;
import eureca.capstone.project.orchestrator.transaction_feed.entity.UserDataCoupon;
import eureca.capstone.project.orchestrator.transaction_feed.repository.custom.UserDataCouponRepositoryCustom;
import eureca.capstone.project.orchestrator.user.entity.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserDataCouponRepositoryImpl implements UserDataCouponRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<UserDataCoupon> findDetailsByUser(User user) {
        return queryFactory
                .selectFrom(userDataCoupon)
                .join(userDataCoupon.dataCoupon, dataCoupon).fetchJoin()
                .join(userDataCoupon.status, status).fetchJoin()
                .leftJoin(dataCoupon.telecomCompany, telecomCompany).fetchJoin()
                .where(userDataCoupon.user.eq(user))
                .fetch();
    }
}
