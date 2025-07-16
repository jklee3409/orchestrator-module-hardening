package eureca.capstone.project.orchestrator.user.repository.impl;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import eureca.capstone.project.orchestrator.user.dto.UserInformationDto;
import eureca.capstone.project.orchestrator.user.repository.custom.UserRepositoryCustom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static eureca.capstone.project.orchestrator.auth.entity.QAuthority.authority;
import static eureca.capstone.project.orchestrator.auth.entity.QRole.role;
import static eureca.capstone.project.orchestrator.auth.entity.QRoleAuthority.roleAuthority;
import static eureca.capstone.project.orchestrator.auth.entity.QUserRole.userRole;
import static eureca.capstone.project.orchestrator.user.entity.QUser.user;


@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public UserInformationDto findUserInformation(String email) {
        log.info("[findUserInformation] 시작 - email: {}", email);

        List<Tuple> result = jpaQueryFactory
                .select(user.userId, user.email, user.password, role.name, authority.name)
                .from(userRole)
                .innerJoin(userRole.user, user)
                .innerJoin(userRole.role, role)
                .innerJoin(roleAuthority).on(roleAuthority.role.eq(role))
                .innerJoin(roleAuthority.authority, authority)
                .where(user.email.eq(email), user.status.code.eq("ACTIVE")) // email, ACTIVE 기준 필터링
                .fetch();

        log.info("[findUserInformation] 쿼리 실행 결과 size: {}", result.size());

        if (result.isEmpty()) {
            log.info("[findUserInformation] 결과 없음 - 빈 DTO 반환");
            return UserInformationDto.emptyDto();
        }

        Long userId = result.get(0).get(user.userId);
        String password = result.get(0).get(user.password);
        String emailFromDB = result.get(0).get(user.email);
        Set<String> roles = new HashSet<>();
        Set<String> authorities = new HashSet<>();

        for (Tuple tuple : result) {
            String roleName = tuple.get(role.name);
            String authorityName = tuple.get(authority.name);
            roles.add(roleName);
            authorities.add(authorityName);
            log.info("[findUserInformation] row - role: {}, authority: {}", roleName, authorityName);
        }

        UserInformationDto userInformationDto = UserInformationDto.builder()
                .userId(userId)
                .password(password)
                .email(emailFromDB)
                .roles(roles)
                .authorities(authorities)
                .build();

        log.info("[findUserInformation] DTO 생성 완료 {}", userInformationDto);

        return userInformationDto;
    }
}
