package eureca.capstone.project.orchestrator.auth.controller;

import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.common.service.EmailVerificationService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/orchestrator")
public class TokenController {
    private final EmailVerificationService emailVerificationService;

    @GetMapping("/verify-email")
    public void verifyEmail(@RequestParam String token, HttpServletResponse httpServletResponse) throws IOException {
        log.info("token: {}", token);
        emailVerificationService.verifyEmailToken(token);
        BaseResponseDto<Void> success = BaseResponseDto.voidSuccess();
        log.info("success: {}", success);
        httpServletResponse.sendRedirect("https://ureca-final.com/");
    }
}
