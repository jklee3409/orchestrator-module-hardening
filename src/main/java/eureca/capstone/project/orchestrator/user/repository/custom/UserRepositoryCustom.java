package eureca.capstone.project.orchestrator.user.repository.custom;

import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.user.dto.UserInformationDto;

public interface UserRepositoryCustom {
    UserInformationDto findUserInformation(String email);
    Long updateStatusByEmail(String email, Status newStatus);
}
