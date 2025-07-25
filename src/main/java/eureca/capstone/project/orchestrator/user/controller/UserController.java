package eureca.capstone.project.orchestrator.user.controller;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.user.dto.request.user.CreateUserRequestDto;
import eureca.capstone.project.orchestrator.user.dto.request.user.UpdateNicknameRequestDto;
import eureca.capstone.project.orchestrator.user.dto.request.user.UpdatePasswordRequestDto;
import eureca.capstone.project.orchestrator.user.dto.request.user.UpdateUserTelecomAndPhoneRequestDto;
import eureca.capstone.project.orchestrator.user.dto.response.user.CreateUserResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user.GetUserProfileResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user.UpdateNicknameResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user.UpdatePasswordResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user.UpdateUserTelecomAndPhoneResponseDto;
import eureca.capstone.project.orchestrator.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "사용자 API", description = "사용자 가입, 프로필 조회/수정 등 사용자 관련 API")
@Slf4j
@RequestMapping("/orchestrator/user")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/")
    @Operation(summary = "사용자 생성 (회원가입)", description = """
            ## 이메일, 비밀번호 등 기본 정보를 받아 새로운 사용자를 생성하고, 이메일 인증 절차를 시작합니다.
            
            ***
            
            ### 📥 요청 바디 (Request Body)
            ```json
            {
              "email": "user@example.com",
              "password": "password123!",
              "phoneNumber": "010-1234-5678",
              "telecomCompanyId": 1,
              "provider": "일반"
            }
            ```
            
            ### 📥 요청 바디 필드 설명
            * `email`: 사용자의 이메일 주소 (문자열)
            * `password`: 사용할 비밀번호 (문자열)
            * `phoneNumber`: 사용자의 전화번호 (문자열, `010-1234-5678` 형식)
            * `telecomCompanyId`: 통신사 ID (1:SKT, 2:KT, 3:LG U+)
            * `provider`: "일반" 으로 고정
            
            ### 🔑 권한
            * 없음 (누구나 호출 가능)
            
            ### ❌ 주요 실패 코드
            * `20004` (EMAIL_ALREADY_EXISTS): 이미 가입된 이메일인 경우
            * `60003` (TELECOM_COMPANY_NOT_FOUND): 존재하지 않는 통신사 ID를 보낸 경우
            * `20002` (USER_CREATE_FAIL): 기타 사용자 등록 중 오류 발생 시
            """)
    public BaseResponseDto<CreateUserResponseDto> createUser(@Valid @RequestBody CreateUserRequestDto createUserRequestDto) {
        CreateUserResponseDto createUserResponseDto = userService.createUser(createUserRequestDto);
        return BaseResponseDto.success(createUserResponseDto);
    }

    @GetMapping("/profile")
    @Operation(summary = "사용자 프로필 조회", description = """
            ## 로그인한 사용자의 닉네임, 이메일, 전화번호, 통신사를 조회합니다.
            
            ***
            
            ### 🔑 권한
            * `ROLE_USER` (사용자 로그인 필요)
            
            ### ❌ 주요 실패 코드
            * `20000` (USER_NOT_FOUND): 토큰에 해당하는 사용자가 존재하지 않을 경우 (정상적인 경우 발생하지 않음)
            """)
    public BaseResponseDto<GetUserProfileResponseDto> getUserProfile(@AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto) {
        GetUserProfileResponseDto getUserProfileResponseDto = userService.getUserProfile(customUserDetailsDto.getEmail());
        return BaseResponseDto.success(getUserProfileResponseDto);
    }

    @PutMapping("/nickname")
    @Operation(summary = "사용자 닉네임 변경", description = """
            ## 로그인한 사용자의 닉네임을 변경합니다.
            
            ***
            
            ### 📥 요청 바디 (Request Body)
            ```json
            {
              "nickname": "새로운닉네임"
            }
            ```
            
            ### 📥 요청 바디 필드 설명
            * `nickname`: 변경할 새로운 닉네임 (문자열, 2글자 이상)
            
            ### 🔑 권한
            * `ROLE_USER` (사용자 로그인 필요)
            
            ### ❌ 주요 실패 코드
            * `20000` (USER_NOT_FOUND): 사용자를 찾을 수 없는 경우
            """)
    public BaseResponseDto<UpdateNicknameResponseDto> updateUserNickname(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @Valid @RequestBody UpdateNicknameRequestDto updateUserNicknameRequestDto
    ) {
        UpdateNicknameResponseDto updateUserNicknameResponseDto = userService.updateUserNickname(customUserDetailsDto.getEmail(), updateUserNicknameRequestDto);
        return BaseResponseDto.success(updateUserNicknameResponseDto);
    }

    @PutMapping("/password")
    @Operation(summary = "사용자 비밀번호 변경", description = """
            ## 현재 비밀번호를 확인한 후, 새로운 비밀번호로 변경합니다.
            
            ***
            
            ### 📥 요청 바디 (Request Body)
            ```json
            {
              "currentPassword": "currentPassword123!",
              "newPassword": "newPassword123!"
            }
            ```
            
            ### 📥 요청 바디 필드 설명
            * `currentPassword`: 현재 사용 중인 비밀번호 (문자열)
            * `newPassword`: 변경할 새로운 비밀번호 (문자열)
            
            ### 🔑 권한
            * `ROLE_USER` (사용자 로그인 필요)
            
            ### ❌ 주요 실패 코드
            * `20000` (USER_NOT_FOUND): 사용자를 찾을 수 없는 경우
            * `20005` (PASSWORD_MISMATCH): 현재 비밀번호가 일치하지 않는 경우
            * `20006` (NEW_PASSWORD_SAME_AS_OLD): 새 비밀번호가 기존 비밀번호와 동일한 경우
            """)
    public BaseResponseDto<UpdatePasswordResponseDto> updateUserPassword(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @Valid @RequestBody UpdatePasswordRequestDto request
    ) {
        UpdatePasswordResponseDto response = userService.updateUserPassword(customUserDetailsDto.getEmail(), request);
        return BaseResponseDto.success(response);
    }

    @PatchMapping("/additional-info")
    @Operation(summary = "사용자 추가 정보 입력 (소셜 로그인)", description = """
            ## 소셜 로그인 후 추가 정보(통신사, 전화번호)가 필요한 사용자가 정보를 입력합니다.
            
            ***
            
            ### 📥 요청 바디 (Request Body)
            ```json
            {
              "telecomCompanyId": 2,
              "phoneNumber": "010-9876-5432"
            }
            ```
            
            ### 📥 요청 바디 필드 설명
            * `telecomCompanyId`: 추가할 통신사의 ID (1:SKT, 2:KT, 3:LG U+)
            * `phoneNumber`: 추가할 전화번호 (문자열, `010-1234-5678` 형식)
            
            ### 🔑 권한
            * `ROLE_USER` (사용자 로그인 필요)
            
            ### ❌ 주요 실패 코드
            * `20000` (USER_NOT_FOUND): 사용자를 찾을 수 없는 경우
            * `60003` (TELECOM_COMPANY_NOT_FOUND): 존재하지 않는 통신사 ID를 보낸 경우
            * `20103` (PLAN_NOT_FOUND): 요금제 정보를 찾을 수 없는 경우 (백엔드 내부 오류가 발생했을 때 이 오류가 발생할 수 있음)
            """)
    public BaseResponseDto<UpdateUserTelecomAndPhoneResponseDto> updateUserTelecomAndPhone(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @RequestBody UpdateUserTelecomAndPhoneRequestDto requestDto
    ) {
        UpdateUserTelecomAndPhoneResponseDto responseDto = userService.updateUserTelecomAndPhone(
                customUserDetailsDto.getUsername(),
                requestDto
        );
        return BaseResponseDto.success(responseDto);
    }

    @GetMapping("/check-email")
    @Operation(summary = "이메일 중복 확인", description = """
            ## 입력된 이메일이 이미 가입된 이메일인지 확인합니다.
            ***
            ### 쿼리 파라미터(Query Parameter)
            * `email`: 중복을 확인할 이메일 주소 (문자열)
            
            ### 응답
            * `data` 필드에 중복 여부를 `boolean` 값으로 반환합니다.
                * `true`: 이미 사용 중인 이메일 (중복)
                * `false`: 사용 가능한 이메일
            
            ### 권한
            * 없음 (누구나 호출 가능)
            
            ### 주요 실패 코드
            * 이 API는 별도의 실패 코드를 반환하기보다는, 성공 응답(`200 OK`)의 `data` 필드에 `true` 또는 `false`를 담아 반환하는 것을 기본으로 합니다.
            
            """)
    public BaseResponseDto<Boolean> checkEmailDuplicate(@RequestParam(name = "email") String email) {
        boolean isDuplicate = userService.checkEmailDuplicate(email);
        return BaseResponseDto.success(isDuplicate);
    }
}
