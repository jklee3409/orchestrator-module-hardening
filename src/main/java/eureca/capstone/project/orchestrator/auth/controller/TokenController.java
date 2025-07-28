package eureca.capstone.project.orchestrator.auth.controller;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.auth.dto.response.ReGenerateTokenResponseDto;
import eureca.capstone.project.orchestrator.auth.service.TokenService;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.common.service.EmailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static eureca.capstone.project.orchestrator.common.constant.UrlConstant.PRODUCT_FRONT_URL;

@Tag(name = "이메일 인증 API", description = "이메일 인증 확인 API (클라이언트 직접 호출X)")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/orchestrator/auth")
public class TokenController {
    private final TokenService tokenService;
    private final EmailVerificationService emailVerificationService;

    @Operation(summary = "이메일 인증 확인 API (클라이언트 직접 호출X)", description = """
            ### 이 API는 클라이언트 애플리케이션에서 직접 호출하는 것이 아니라, 사용자가 회원가입 시 수신한 이메일의 '인증하기' 링크를 클릭했을 때 호출되는 엔드포인트입니다.
            
            사용자가 이메일의 링크를 클릭하면, 서버는 URL에 포함된 토큰을 검증하고 사용자의 이메일 인증 상태를 업데이트합니다.
            
            ***
            
            ### 📥 요청 파라미터 (Query Parameters)
            | 이름 | 타입 | 필수 | 설명 |
            |---|---|:---:|---|
            | `token` | `String` | O | 이메일로 전송된 고유 인증 토큰 |
            
            ### 🔑 권한
            * 모든 사용자 (토큰의 유효성으로 보호됨)
            
            ### ❌ 주요 실패 코드
            * `10006` (EMAIL_TOKEN_MISMATCH): 유효하지 않거나 만료된 토큰일 경우 발생합니다.
            """)
    @GetMapping("/verify-email")
    public void verifyEmail(
            @Parameter(description = "이메일로 전송된 고유 인증 토큰") @RequestParam String token,
            HttpServletResponse httpServletResponse
    ) throws IOException {
        log.info("token: {}", token);
        emailVerificationService.verifyEmailToken(token);
        BaseResponseDto<Void> success = BaseResponseDto.voidSuccess();
        log.info("success: {}", success);
        httpServletResponse.sendRedirect(PRODUCT_FRONT_URL);
    }

    @Operation(
            summary = "Token 재발급 API", description = """
            ### 📌 설명  
            서버에 저장된 **Refresh Token**(쿠키 기반)을 이용하여 새로운 **Token**을 재발급합니다.  
            클라이언트는 기존 Access Token이 만료되었을 때(10001 에러) 이 API를 호출하여 갱신할 수 있습니다.
            
            ---
            
            ### 📥 요청 쿠키
            | 이름           | 타입     | 필수 | 설명                          |
            |----------------|----------|:----:|-------------------------------|
            | `refreshToken` | `String` | O    | 서버에 저장된 리프레시 토큰 |
            
            ---
            
            ### 📤 응답
            | 필드          | 타입     | 설명                         |
            |---------------|----------|------------------------------|
            | `accessToken` | `String` | 재발급된 새로운 액세스 토큰 |
            
            ---
            
            ### 🔑 권한
            * 모든 사용자 (쿠키에 저장된 refreshToken 기반)
            
            ---
            
            ### ❌ 주요 실패 코드
            * **HTTP 401 Unauthorized**: 유효하지 않은 refreshToken 값을 보냈을 경우
            
            ---
            
            ### 📝 예시
            **Request**
            ```
            GET /re-generate-token
            Cookie: refreshToken={your_refresh_token}
            ```
            
            **Response**
            ```json
            {
              "statusCode": 200,
              "message": "success",
              "data": {
                "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
              }
            }
            ```
            """
    )
    @GetMapping("/re-generate-token")
    public BaseResponseDto<ReGenerateTokenResponseDto> reGenerateToken(@AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto, HttpServletResponse httpServletResponse) throws IOException {
        log.info("reGenerateToken customUserDetailsDto: {}", customUserDetailsDto);
        String accessToken = tokenService.reGenerateToken(customUserDetailsDto, httpServletResponse);
        ReGenerateTokenResponseDto reGenerateTokenResponseDto = ReGenerateTokenResponseDto.builder()
                .accessToken(accessToken)
                .build();
        log.info("reGenerateTokenResponseDto: {}", reGenerateTokenResponseDto);
        BaseResponseDto<ReGenerateTokenResponseDto> success = BaseResponseDto.success(reGenerateTokenResponseDto);
        log.info("success: {}", success);
        return success;
    }
}