package eureca.capstone.project.orchestrator.user.service;

import eureca.capstone.project.orchestrator.user.dto.request.user.CreateUserRequestDto;
import eureca.capstone.project.orchestrator.user.dto.request.user.GetUserProfileRequestDto;
import eureca.capstone.project.orchestrator.user.dto.request.user.UpdateNicknameRequestDto;
import eureca.capstone.project.orchestrator.user.dto.request.user.UpdatePasswordRequestDto;
import eureca.capstone.project.orchestrator.user.dto.response.user.CreateUserResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user.GetUserCountResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user.GetUserProfileResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user.UpdateNicknameResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user.UpdatePasswordResponseDto;

public interface UserService {
    CreateUserResponseDto createUser(CreateUserRequestDto createUserRequestDto);
    GetUserProfileResponseDto getUserProfile(GetUserProfileRequestDto getUserProfileRequestDto);
    UpdateNicknameResponseDto updateUserNickname(UpdateNicknameRequestDto updateUserNicknameRequestDto);
    UpdatePasswordResponseDto updateUserPassword(UpdatePasswordRequestDto updatePasswordRequestDto);
    GetUserCountResponseDto getUserCount();
}
