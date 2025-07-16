package eureca.capstone.project.orchestrator.user.repository;

import eureca.capstone.project.orchestrator.user.entity.UserData;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface UserDataRepository extends JpaRepository<UserData, Long> {
}
