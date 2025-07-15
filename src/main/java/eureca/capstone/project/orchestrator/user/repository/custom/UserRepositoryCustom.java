package eureca.capstone.project.orchestrator.user.repository.custom;

import eureca.capstone.project.orchestrator.user.dto.UserInformationDto;

public interface UserRepositoryCustom {
    UserInformationDto findUserInformation(String email);
}
