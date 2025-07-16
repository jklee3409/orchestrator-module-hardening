package eureca.capstone.project.orchestrator.user.repository.impl;

import static eureca.capstone.project.orchestrator.user.entity.QUserData.userData;

import com.querydsl.jpa.impl.JPAQueryFactory;
import eureca.capstone.project.orchestrator.user.entity.UserData;
import eureca.capstone.project.orchestrator.user.repository.custom.UserDataRepositoryCustom;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserDataRepositoryImpl implements UserDataRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<UserData> findByUserIdWithLock(Long userId) {
        UserData result = jpaQueryFactory
                .selectFrom(userData)
                .where(userData.userId.eq(userId))
                .setLockMode(LockModeType.PESSIMISTIC_READ)
                .fetchOne();

        return Optional.ofNullable(result);
    }
}
