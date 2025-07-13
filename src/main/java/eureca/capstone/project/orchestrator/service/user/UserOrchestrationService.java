package eureca.capstone.project.orchestrator.service.user;

import eureca.capstone.project.orchestrator.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.dto.request.orchestrator.SignUpRequestDto;
import eureca.capstone.project.orchestrator.dto.response.user.CreateUserResponseDto;

public interface UserOrchestrationService {
    BaseResponseDto<CreateUserResponseDto> signup(SignUpRequestDto signUpRequestDto);
}
