package eureca.capstone.project.orchestrator.auth.controller;

import static eureca.capstone.project.orchestrator.common.constant.RedisConstant.REDIS_REFRESH_TOKEN;

import eureca.capstone.project.orchestrator.auth.dto.response.LoginResponseDto;
import eureca.capstone.project.orchestrator.auth.util.CookieUtil;
import eureca.capstone.project.orchestrator.auth.util.JwtUtil;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.common.service.RedisService;
import eureca.capstone.project.orchestrator.user.dto.UserInformationDto;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/orchestrator/oauth")
@RequiredArgsConstructor
public class OAuthController {
    private final RedisService redisService;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final UserRepository userRepository;

    @PostMapping("/token")
    public BaseResponseDto<LoginResponseDto> exchangeToken(@RequestBody Map<String, String> payload, HttpServletResponse httpServletResponse) {
        String authCode = payload.get("authCode");
        String redisKey = "oauth-temp-token:" + authCode;

        @SuppressWarnings("unchecked")
        Map<String, Object> tokenData = (Map<String, Object>) redisService.getValue(redisKey);

        if (tokenData == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않거나 만료된 인증 코드입니다.");
        }

        redisService.deleteValue(redisKey); // 임시 코드 사용 후 즉시 삭제

        String email = (String) tokenData.get("email");
        boolean isNewUser = (boolean) tokenData.get("isNewUser");

        // 사용자 정보 = 역할 권한 조회
        UserInformationDto userInformationDto = userRepository.findUserInformation(email);
        Long userId = userInformationDto.getUserId();
        Set<String> roles = userInformationDto.getRoles();
        Set<String> authorities = userInformationDto.getAuthorities();

        // JWT 토큰 발급
        String accessToken = jwtUtil.generateAccessToken(email, roles, authorities, userId);
        String refreshToken = jwtUtil.generateRefreshToken(email, roles, authorities, userId);
        log.info("[onAuthenticationSuccess] - accessToken: {}", accessToken);
        log.info("[onAuthenticationSuccess] - refreshToken: {}", refreshToken);

        // Refresh 토큰 Response 헤더에 할당 및 레디스에 저장 (14일 보관)
        redisService.setValue(REDIS_REFRESH_TOKEN + userId, refreshToken, Duration.ofDays(14));
        cookieUtil.createRefreshTokenCookie(refreshToken, httpServletResponse);

        // 응답 객체 생성
        LoginResponseDto loginResponse = LoginResponseDto.builder()
                .accessToken(accessToken)
                .isNewUser(isNewUser)
                .build();

        return BaseResponseDto.success(loginResponse);
    }
}
