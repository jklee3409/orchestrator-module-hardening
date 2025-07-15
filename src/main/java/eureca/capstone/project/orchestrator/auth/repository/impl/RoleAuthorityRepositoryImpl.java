package eureca.capstone.project.orchestrator.auth.repository.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import eureca.capstone.project.orchestrator.auth.entity.Authority;
import eureca.capstone.project.orchestrator.auth.entity.Role;
import eureca.capstone.project.orchestrator.auth.repository.custom.RoleAuthorityRepositoryCustom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import static eureca.capstone.project.orchestrator.auth.entity.QRoleAuthority.roleAuthority;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RoleAuthorityRepositoryImpl implements RoleAuthorityRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public boolean existsByRoleAndAuthority(Role role, Authority auth) {
        return jpaQueryFactory
                .selectOne()
                .from(roleAuthority)
                .where(
                        roleAuthority.role.eq(role),
                        roleAuthority.authority.eq(auth)
                )
                .fetchFirst() != null;
    }
}
