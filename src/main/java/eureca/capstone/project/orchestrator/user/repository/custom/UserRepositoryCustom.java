package eureca.capstone.project.orchestrator.user.repository.custom;

import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.user.dto.UserInformationDto;
import eureca.capstone.project.orchestrator.user.entity.User;

import java.util.Optional;

public interface UserRepositoryCustom {
    UserInformationDto findUserInformation(String email);
    Long updateStatusByEmail(String email, Status newStatus);
    Optional<User> findActiveUserByEmail(String email);
}
