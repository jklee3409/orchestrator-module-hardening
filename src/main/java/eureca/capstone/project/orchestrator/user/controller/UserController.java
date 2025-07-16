package eureca.capstone.project.orchestrator.user.controller;

import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.user.dto.request.user.CreateUserRequestDto;
import eureca.capstone.project.orchestrator.user.dto.request.user.UpdateNicknameRequestDto;
import eureca.capstone.project.orchestrator.user.dto.request.user.UpdatePasswordRequestDto;
import eureca.capstone.project.orchestrator.user.dto.response.user.CreateUserResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user.GetUserCountResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user.GetUserProfileResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user.UpdateNicknameResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user.UpdatePasswordResponseDto;
import eureca.capstone.project.orchestrator.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    @Operation(summary = "사용자 프로필 조회", description = "로그인한 사용자의 닉네임, 이메일, 전화번호, 통신사를 반환합니다.")
    public BaseResponseDto<GetUserProfileResponseDto> getUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        GetUserProfileResponseDto getUserProfileResponseDto = userService.getUserProfile(userDetails.getUsername());
        return BaseResponseDto.success(getUserProfileResponseDto);
    }

    @PutMapping("/nickname")
    @Operation(summary = "사용자 닉네임 변경", description = "로그인한 사용자의 닉네임을 변경합니다.")
    public BaseResponseDto<UpdateNicknameResponseDto> updateUserNickname(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateNicknameRequestDto updateUserNicknameRequestDto
    ) {
        UpdateNicknameResponseDto updateUserNicknameResponseDto = userService.updateUserNickname(userDetails.getUsername(), updateUserNicknameRequestDto);
        return BaseResponseDto.success(updateUserNicknameResponseDto);
    }

    @PutMapping("/password")
    @Operation(summary = "사용자 비밀번호 변경", description = "로그인한 사용자의 비밀번호를 변경합니다.")
    public BaseResponseDto<UpdatePasswordResponseDto> updateUserPassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdatePasswordRequestDto request
    ) {
        UpdatePasswordResponseDto response = userService.updateUserPassword(userDetails.getUsername(), request);
        return BaseResponseDto.success(response);
    }

    @GetMapping("/count")
    @Operation(summary = "전체 및 당일 가입자 수 조회", description = "전체 활성 가입자 수와 당일 가입자 수를 반환합니다.")
    public BaseResponseDto<GetUserCountResponseDto> getUserCount() {
        GetUserCountResponseDto userCountResponseDto = userService.getUserCount();
        return BaseResponseDto.success(userCountResponseDto);
    }
}
