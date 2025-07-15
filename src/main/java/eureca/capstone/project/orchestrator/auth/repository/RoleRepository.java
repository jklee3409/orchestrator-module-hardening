package eureca.capstone.project.orchestrator.auth.repository;

import eureca.capstone.project.orchestrator.auth.entity.Role;
import eureca.capstone.project.orchestrator.auth.repository.custom.RoleRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long>, RoleRepositoryCustom {
}
