package eureca.capstone.project.orchestrator.user.service;

import eureca.capstone.project.orchestrator.user.dto.request.user_data.CreateUserDataRequestDto;
import eureca.capstone.project.orchestrator.user.dto.response.user_data.CreateUserDataResponseDto;

public interface UserDataService {
    CreateUserDataResponseDto createUserData(CreateUserDataRequestDto createUserDataRequestDto);

}
