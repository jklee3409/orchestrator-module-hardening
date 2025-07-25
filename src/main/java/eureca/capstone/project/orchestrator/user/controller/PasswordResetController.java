package eureca.capstone.project.orchestrator.user.controller;

import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.user.dto.request.user.PasswordResetConfirmRequest;
import eureca.capstone.project.orchestrator.user.dto.request.user.PasswordResetRequest;
import eureca.capstone.project.orchestrator.user.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Tag(name = "비밀번호 찾기 API", description = "비밀번호 찾기 관련 API")
@Slf4j
@Controller
@RequestMapping("/orchestrator/user/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @Operation(summary = "비밀번호 재설정 요청 (API)", description = """
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
    @ResponseBody
    public BaseResponseDto<String> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        passwordResetService.requestPasswordReset(request.getEmail());
        return BaseResponseDto.success("입력하신 이메일로 비밀번호 재설정 안내 메일을 발송했습니다. 메일이 오지 않으면 스팸함을 확인해주세요.");
    }

    @Operation(summary = "비밀번호 재설정 폼 페이지 요청 (View)", description = "이메일로 발송된 링크를 통해 비밀번호 재설정 폼 페이지를 요청합니다.")
    @GetMapping("/form")
    public String showPasswordResetForm(@RequestParam("token") String token, Model model) {
        if (!passwordResetService.isTokenValid(token)) {
            model.addAttribute("message", "유효하지 않거나 만료된 링크입니다.");
            return "password-reset-error"; // 에러 페이지 View 이름
        }

        model.addAttribute("token", token);
        return "password-reset-form"; // 비밀번호 재설정 폼 View 이름
    }

    @PostMapping("/confirm")
    @ResponseBody
    public BaseResponseDto<String> confirmPasswordReset(@Valid @RequestBody
                                                        PasswordResetConfirmRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return BaseResponseDto.success("비밀번호가 성공적으로 변경되었습니다.");
    }

}
