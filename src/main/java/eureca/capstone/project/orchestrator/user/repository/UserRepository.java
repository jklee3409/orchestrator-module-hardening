package eureca.capstone.project.orchestrator.user.repository;

import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.custom.UserRepositoryCustom;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {
    Optional<User> findByEmail(String email);

    long countByStatus(Status status);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
