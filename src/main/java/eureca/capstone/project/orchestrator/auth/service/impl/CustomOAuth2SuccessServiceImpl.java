package eureca.capstone.project.orchestrator.auth.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import eureca.capstone.project.orchestrator.auth.dto.response.LoginResponseDto;
import eureca.capstone.project.orchestrator.auth.util.CookieUtil;
import eureca.capstone.project.orchestrator.auth.util.JwtUtil;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.common.service.RedisService;
import eureca.capstone.project.orchestrator.user.dto.UserInformationDto;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

import static eureca.capstone.project.orchestrator.common.constant.RedisConstant.REDIS_REFRESH_TOKEN;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2SuccessServiceImpl implements AuthenticationSuccessHandler {
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final UserRepository userRepository;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) {
        // 요청값 출력 및 키값 추출
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Long userId = oAuth2User.getAttribute("userId");
        String email = oAuth2User.getAttribute("email");
        log.info("[onAuthenticationSuccess] - userId: {}", userId);
        log.info("[onAuthenticationSuccess] - email: {}", email);

        // 사용자 정보 = 역할 권한 조회
        UserInformationDto userInformationDto = userRepository.findUserInformation(email);
        Set<String> roles = userInformationDto.getRoles();
        Set<String> authorities = userInformationDto.getAuthorities();

        // JWT 토큰 발급
        String accessToken = jwtUtil.generateAccessToken(email, roles, authorities, userId);
        String refreshToken = jwtUtil.generateRefreshToken(email, roles, authorities, userId);
        log.info("[onAuthenticationSuccess] - accessToken: {}", accessToken);
        log.info("[onAuthenticationSuccess] - refreshToken: {}", refreshToken);

        // Refresh 토큰 Response 헤더에 할당 및 레디스에 저장 (14일 보관)
        cookieUtil.createRefreshTokenCookie(refreshToken, httpServletResponse);
        redisService.setValue(REDIS_REFRESH_TOKEN + userId, refreshToken, Duration.ofDays(14));

        // 응답 객체 생성
        BaseResponseDto<LoginResponseDto> success = BaseResponseDto.success(
                LoginResponseDto.builder()
                        .accessToken(accessToken)
                        .build()
        );

        // 객체 json 으로 변환 후 반환
        writeJsonResponse(httpServletResponse, success);
    }

    public void writeJsonResponse(HttpServletResponse httpServletResponse, Object dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            httpServletResponse.setStatus(HttpServletResponse.SC_OK); // 200
            httpServletResponse.setContentType("application/json");
            httpServletResponse.setCharacterEncoding("UTF-8");
            httpServletResponse.getWriter().write(json);
        } catch (Exception e) {
            throw new RuntimeException("JSON 응답 처리 실패", e);
        }
    }
}
