package eureca.capstone.project.orchestrator.user.repository.custom;

import eureca.capstone.project.orchestrator.user.entity.UserData;
import java.util.Optional;

public interface UserDataRepositoryCustom {
    Optional<UserData> findByUserIdWithLock(Long userId);
}
