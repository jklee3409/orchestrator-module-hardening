package eureca.capstone.project.orchestrator.auth.repository.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import eureca.capstone.project.orchestrator.auth.entity.Role;
import eureca.capstone.project.orchestrator.auth.repository.custom.RoleRepositoryCustom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import static eureca.capstone.project.orchestrator.auth.entity.QRole.role;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RoleRepositoryImpl implements RoleRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Role findRoleByName(String name) {
        return jpaQueryFactory
                .select(role)
                .from(role)
                .where(role.name.eq(name))
                .fetchOne();
    }
}
