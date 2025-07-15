package eureca.capstone.project.orchestrator.auth.repository;

import eureca.capstone.project.orchestrator.auth.entity.RoleAuthority;
import eureca.capstone.project.orchestrator.auth.repository.custom.RoleAuthorityRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleAuthorityRepository extends JpaRepository<RoleAuthority, Long>, RoleAuthorityRepositoryCustom {

}
