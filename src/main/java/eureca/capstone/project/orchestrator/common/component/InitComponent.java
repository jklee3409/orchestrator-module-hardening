package eureca.capstone.project.orchestrator.common.component;

import eureca.capstone.project.orchestrator.auth.entity.Authority;
import eureca.capstone.project.orchestrator.auth.entity.Role;
import eureca.capstone.project.orchestrator.auth.entity.RoleAuthority;
import eureca.capstone.project.orchestrator.auth.repository.AuthorityRepository;
import eureca.capstone.project.orchestrator.auth.repository.RoleAuthorityRepository;
import eureca.capstone.project.orchestrator.auth.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InitComponent {
    private final RoleRepository roleRepository;
    private final AuthorityRepository authorityRepository;
    private final RoleAuthorityRepository roleAuthorityRepository;

    /**
     * 애플리케이션 시작 시, Role × Authority 의 모든 조합을 확인해서
     * DB에 존재하지 않는 매핑만 RoleAuthority 로 저장한다.
     */
    @PostConstruct
    @Transactional
    public void init() {
        List<Role> roles = roleRepository.findAll();
        List<Authority> auths = authorityRepository.findAll();

        for (Role role : roles) {
            for (Authority auth : auths) {
                // 이미 매핑이 있는지 체크
                boolean exists = roleAuthorityRepository.existsByRoleAndAuthority(role, auth);
                if (!exists) {
                    RoleAuthority ra = RoleAuthority.builder()
                            .role(role)
                            .authority(auth)
                            .build();
                    roleAuthorityRepository.save(ra);
                }
            }
        }
    }
}
