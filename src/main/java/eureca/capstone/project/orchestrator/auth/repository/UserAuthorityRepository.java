package eureca.capstone.project.orchestrator.auth.repository;

import eureca.capstone.project.orchestrator.auth.entity.UserAuthority;
import eureca.capstone.project.orchestrator.auth.repository.custom.UserAuthorityRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAuthorityRepository extends JpaRepository<UserAuthority, Long>, UserAuthorityRepositoryCustom {
}
