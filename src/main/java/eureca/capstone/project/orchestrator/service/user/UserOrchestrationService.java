package eureca.capstone.project.orchestrator.service.user;

import eureca.capstone.project.orchestrator.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.dto.request.orchestrator.SignUpRequestDto;
import eureca.capstone.project.orchestrator.dto.request.user.UpdateUserPasswordRequestDto;
import eureca.capstone.project.orchestrator.dto.response.user.CreateUserResponseDto;
import eureca.capstone.project.orchestrator.dto.response.user.UpdateUserPasswordResponseDto;

public interface UserOrchestrationService {
    BaseResponseDto<CreateUserResponseDto> signup(SignUpRequestDto signUpRequestDto);
    BaseResponseDto<UpdateUserPasswordResponseDto> updateUserPassword(UpdateUserPasswordRequestDto updateUserPasswordRequestDto);
}
