package eureca.capstone.project.orchestrator.auth.repository.custom;

import eureca.capstone.project.orchestrator.auth.entity.Role;

public interface RoleRepositoryCustom {
    Role findRoleByName(String name);
}
