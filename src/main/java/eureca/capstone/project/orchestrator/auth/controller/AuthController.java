package eureca.capstone.project.orchestrator.auth.controller;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.auth.dto.request.LoginRequestDto;
import eureca.capstone.project.orchestrator.auth.dto.response.LoginResponseDto;
import eureca.capstone.project.orchestrator.auth.service.TokenService;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.common.exception.custom.BlackListUserException;
import eureca.capstone.project.orchestrator.common.service.RedisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static eureca.capstone.project.orchestrator.common.constant.RedisConstant.REDIS_BLACK_LIST_USER;
import static eureca.capstone.project.orchestrator.common.constant.RedisConstant.REDIS_REFRESH_TOKEN;

@Tag(name = "인증 API", description = "사용자 로그인, 로그아웃 등 인증 관련 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/orchestrator/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final RedisService redisService;

    @Operation(summary = "사용자 로그인 API", description = """
            ## 이메일과 비밀번호로 사용자를 인증하고 JWT 토큰을 발급합니다.
            로그인 성공 시, **Access Token**은 응답 바디에, **Refresh Token**은 `HttpOnly` 쿠키에 담아 반환합니다.
            
            ***
            
            ### 📥 요청 바디 (Request Body)
            ```json
            {
              "email": "user@example.com",
              "password": "password123"
            }
            ```
            
            ### 🔑 권한
            * 모든 사용자
            
            ### ❌ 주요 실패 코드
            * `401 Unauthorized`: 이메일 또는 비밀번호가 일치하지 않을 경우 Spring Security가 반환하는 표준 응답입니다.
            
            ### 📝 참고 사항
            * 클라이언트는 응답으로 받은 **Access Token**을 저장하고, 인증이 필요한 모든 API 요청의 `Authorization` 헤더에 `Bearer <token>` 형식으로 전송해야 합니다.
            """)
    @PostMapping("/login")
    public BaseResponseDto<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse httpServletResponse) {
        // 사용자 정보 검증 및 로그 출력
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getEmail(),
                        loginRequestDto.getPassword()
                )
        );
        log.info("authentication: {}", authentication);

        CustomUserDetailsDto customUserDetailsDto = (CustomUserDetailsDto) authentication.getPrincipal();

        // 혹시 제제 대상인 사용자라면, 이미 로그인시에 필요한 제제권한을 제외한 권한을 갖게 되므로, 예외를 터트려서 로그인 페이지로 이동할 필요가 없음
        // 해서, 해당 레디스 값을 삭제 처리함.
        if (redisService.hasKey(REDIS_BLACK_LIST_USER + customUserDetailsDto.getUserId())) {
            redisService.deleteValue(REDIS_BLACK_LIST_USER + customUserDetailsDto.getUserId());
            log.error("[JwtFilter] Blacklisted user delete key. key: {}", REDIS_BLACK_LIST_USER + customUserDetailsDto.getUserId());
        }

        // JWT 토큰 발급 (AccessToken, RefreshToken)
        String accessToken = tokenService.generateToken(
                customUserDetailsDto,
                httpServletResponse
        );

        // 리턴 객체 생성 및 로그 출력
        LoginResponseDto loginResponseDto = LoginResponseDto.builder()
                .accessToken(accessToken)
                .userId(customUserDetailsDto.getUserId())
                .build();
        BaseResponseDto<LoginResponseDto> success = BaseResponseDto.success(loginResponseDto);
        log.info("success: {}", success);

        // return
        return success;
    }

    @Operation(summary = "사용자 로그아웃 API", description = """
            ## 현재 로그인된 사용자를 로그아웃 처리합니다.
            서버에 저장된 사용자의 Refresh Token을 삭제하여 현재 세션을 무효화시킵니다.
            
            ***
            
            ### 📥 요청 파라미터
            * 별도의 요청 바디는 없으며, `Authorization` 헤더에 유효한 Access Token을 포함하여 요청해야 합니다.
            
            ### 🔑 권한
            * `ROLE_USER` (사용자 로그인 필요)
            
            ### ❌ 주요 실패 코드
            * `401 Unauthorized`: 유효하지 않거나 만료된 Access Token으로 요청할 경우 발생합니다.
            """)
    @PostMapping("/logout")
    public BaseResponseDto<Void> logout(@AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto) {
        // 요청 값 로그 출력
        log.info("customUserDetailsDto: {}", customUserDetailsDto);
        // Refresh Token 삭제
        redisService.deleteValue(REDIS_REFRESH_TOKEN + customUserDetailsDto.getUserId());
        // 반환값 생성 및 출력
        BaseResponseDto<Void> success = BaseResponseDto.voidSuccess();
        log.info("success: {}", success);
        // 응답값 반환
        return success;
    }
}