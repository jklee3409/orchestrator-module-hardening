package eureca.capstone.project.orchestrator.user.repository;

import eureca.capstone.project.orchestrator.user.entity.UserData;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDataRepository extends JpaRepository<UserData, Long> {
    Optional<UserData> findByUserId(Long userId);
}
