package eureca.capstone.project.orchestrator.controller.user;

import eureca.capstone.project.orchestrator.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.dto.request.orchestrator.SignUpRequestDto;
import eureca.capstone.project.orchestrator.dto.response.user.CreateUserResponseDto;
import eureca.capstone.project.orchestrator.service.user.UserOrchestrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/orchestrator")
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserOrchestrationService userOrchestrationService;

    @PostMapping("/signup")
    public BaseResponseDto<CreateUserResponseDto> signup(@RequestBody SignUpRequestDto signUpRequestDto) {
        BaseResponseDto<CreateUserResponseDto> response = userOrchestrationService.signup(signUpRequestDto);
//        if (response.getStatusCode() != 200) throw new NotSuccessStatusCodeException(); // client 에서 코드 관리
        return response;
    }
}
