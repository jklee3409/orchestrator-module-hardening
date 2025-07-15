package eureca.capstone.project.orchestrator.auth.repository.custom;

import eureca.capstone.project.orchestrator.auth.entity.Authority;
import eureca.capstone.project.orchestrator.auth.entity.Role;

public interface RoleAuthorityRepositoryCustom {
    boolean existsByRoleAndAuthority(Role role, Authority auth);
}
