package eureca.capstone.project.orchestrator.user.controller;

import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.user.dto.request.user.PasswordResetConfirmRequest;
import eureca.capstone.project.orchestrator.user.dto.request.user.PasswordResetRequest;
import eureca.capstone.project.orchestrator.user.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Tag(name = "비밀번호 찾기 API", description = "비밀번호 찾기 관련 API")
@Slf4j
@RestController
@RequestMapping("/orchestrator/user/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @Operation(summary = "비밀번호 재설정 링크 요청 API", description = """
            ## 사용자가 이메일을 입력하여 비밀번호 재설정 링크를 요청합니다.
            
            ***
            
            ### 📥 요청 바디 (Request Body)
            ```json
            {
              "email": "user@example.com"
            }
            ```
            
            ### ✅ 응답
            * 이메일 존재 여부와 상관없이 항상 성공 응답을 반환하여, 가입 여부를 추측할 수 없도록 합니다.
            * 실제 이메일이 존재할 경우에만 재설정 링크가 포함된 메일이 발송됩니다.
            """)
    @PostMapping("/request")
    public BaseResponseDto<String> requestPasswordReset(@RequestBody PasswordResetRequest request) {
        passwordResetService.requestPasswordReset(request.getEmail());
        return BaseResponseDto.success("입력하신 이메일로 비밀번호 재설정 안내 메일을 발송했습니다. 메일이 오지 않으면 스팸함을 확인해주세요.");
    }

    @Operation(summary = "비밀번호 재설정 후 비밀번호 변경 API", description = """
            ## 사용자가 비밀번호 변경 페이지에서 새로운 비밀번호를 입력하고 토큰과 함께 전송하면 토큰 검증 후 비밀번호가 변경됩니다.
            
            ***
            
            ### 📥 요청 바디 (Request Body)
            ```json
            {
              "token":"url에 있는 토큰값",
              "newPassword": "12345678"
            }
            ```
            
            ### ❌ 주요 실패 코드
            * `20007` (PASSWORD_RESET_LINK_EXPIRED): 유효하지 않거나 만료된 비밀번호 재설정 토큰일 경우
            """)
    @PostMapping("/confirm")
    public BaseResponseDto<String> confirmPasswordReset(@RequestBody PasswordResetConfirmRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return BaseResponseDto.success("비밀번호가 성공적으로 변경되었습니다.");
    }

}
