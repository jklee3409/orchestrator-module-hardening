package eureca.capstone.project.orchestrator.auth.repository.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import eureca.capstone.project.orchestrator.auth.entity.UserAuthority;
import eureca.capstone.project.orchestrator.auth.repository.custom.UserAuthorityRepositoryCustom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

import static eureca.capstone.project.orchestrator.auth.entity.QUserAuthority.userAuthority;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserAuthorityRepositoryImpl implements UserAuthorityRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<UserAuthority> findUserAuthorityByUserId(Long userId) {
        return jpaQueryFactory
                .selectFrom(userAuthority)
                .join(userAuthority.authority).fetchJoin()
                .where(userAuthority.user.userId.eq(userId))
                .fetch();
    }
}
