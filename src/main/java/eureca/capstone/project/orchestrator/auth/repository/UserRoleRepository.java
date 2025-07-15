package eureca.capstone.project.orchestrator.auth.repository;

import eureca.capstone.project.orchestrator.auth.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
}
