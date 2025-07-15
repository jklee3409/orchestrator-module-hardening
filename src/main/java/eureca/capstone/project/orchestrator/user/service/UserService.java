package eureca.capstone.project.orchestrator.user.service;

import eureca.capstone.project.orchestrator.user.dto.request.user.CreateUserRequestDto;
import eureca.capstone.project.orchestrator.user.dto.request.user.GetUserProfileRequestDto;
import eureca.capstone.project.orchestrator.user.dto.response.user.CreateUserResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user.GetUserProfileResponseDto;

public interface UserService {
    CreateUserResponseDto createUser(CreateUserRequestDto createUserRequestDto);
    GetUserProfileResponseDto getUserProfile(GetUserProfileRequestDto getUserProfileRequestDto);
}
