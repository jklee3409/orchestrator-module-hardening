package eureca.capstone.project.orchestrator.auth.repository;

import eureca.capstone.project.orchestrator.auth.entity.UserAuthority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAuthorityRepository extends JpaRepository<UserAuthority, Long> {
}
