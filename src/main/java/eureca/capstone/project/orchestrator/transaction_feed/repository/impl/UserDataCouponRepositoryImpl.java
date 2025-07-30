package eureca.capstone.project.orchestrator.transaction_feed.repository.impl;

import static eureca.capstone.project.orchestrator.common.entity.QStatus.status;
import static eureca.capstone.project.orchestrator.common.entity.QTelecomCompany.telecomCompany;
import static eureca.capstone.project.orchestrator.transaction_feed.entity.QDataCoupon.dataCoupon;
import static eureca.capstone.project.orchestrator.transaction_feed.entity.QUserDataCoupon.userDataCoupon;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import eureca.capstone.project.orchestrator.transaction_feed.entity.UserDataCoupon;
import eureca.capstone.project.orchestrator.transaction_feed.repository.custom.UserDataCouponRepositoryCustom;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.entity.UserData;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserDataCouponRepositoryImpl implements UserDataCouponRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<UserDataCoupon> findDetailsByUser(User user, Pageable pageable) {
        // 1. 최우선 정렬: 'ISSUED' 상태를 가장 먼저
        OrderSpecifier<Integer> statusPriorityOrder = new CaseBuilder()
                .when(status.code.eq("ISSUED")).then(1)
                .otherwise(2) // ISSUED가 아닌 모든 상태는 2순위
                .asc();

        // 2. 보조 정렬: 만료일이 임박한 순서 (오름차순)
        OrderSpecifier<LocalDateTime> expiryOrder = userDataCoupon.expiresAt.asc();

        List<UserDataCoupon> content = queryFactory
                .selectFrom(userDataCoupon)
                .join(userDataCoupon.dataCoupon, dataCoupon).fetchJoin()
                .join(userDataCoupon.status, status).fetchJoin()
                .leftJoin(dataCoupon.telecomCompany, telecomCompany).fetchJoin()
                .where(userDataCoupon.user.eq(user))
                .orderBy(statusPriorityOrder, expiryOrder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(userDataCoupon.count())
                .from(userDataCoupon)
                .where(userDataCoupon.user.eq(user))
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }

    @Override
    public Optional<UserDataCoupon> findDetailsById(Long userDataCouponId) {
        UserDataCoupon result = queryFactory
                .selectFrom(userDataCoupon)
                .join(userDataCoupon.dataCoupon, dataCoupon).fetchJoin()
                .join(userDataCoupon.user).fetchJoin()
                .join(userDataCoupon.status, status).fetchJoin()
                .leftJoin(dataCoupon.telecomCompany, telecomCompany).fetchJoin()
                .where(userDataCoupon.userDataCouponId.eq(userDataCouponId))
                .fetchOne();

        return Optional.ofNullable(result);
    }
}
