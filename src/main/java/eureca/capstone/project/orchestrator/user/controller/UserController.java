package eureca.capstone.project.orchestrator.user.controller;

import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.user.dto.request.user.CreateUserRequestDto;
import eureca.capstone.project.orchestrator.user.dto.request.user.GetUserProfileRequestDto;
import eureca.capstone.project.orchestrator.user.dto.response.user.CreateUserResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user.GetUserProfileResponseDto;
import eureca.capstone.project.orchestrator.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/user")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/")
    @Operation(summary = "사용자 생성", description = "새로운 사용자를 생성합니다.")
    public BaseResponseDto<CreateUserResponseDto> createUser(@Valid @RequestBody CreateUserRequestDto createUserRequestDto) {
        CreateUserResponseDto createUserResponseDto = userService.createUser(createUserRequestDto);
        return BaseResponseDto.success(createUserResponseDto);
    }

    @GetMapping("/profile")
    @Operation(summary = "사용자 프로필 조회", description = "userId 에 해당하는 사용자의 닉네임, 이메일, 전화번호, 통신사를 반환합니다.")
    public BaseResponseDto<GetUserProfileResponseDto> getUserProfile(@Valid GetUserProfileRequestDto getUserProfileRequestDto) {
        GetUserProfileResponseDto getUserProfileResponseDto = userService.getUserProfile(getUserProfileRequestDto);
        return BaseResponseDto.success(getUserProfileResponseDto);
    }
}
