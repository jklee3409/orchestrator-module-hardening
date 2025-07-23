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
import java.io.IOException;
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

    private static final String REDIRECT_URI = "https://ureca-final.com/oauth/callback";
    private static final String LOCAL_REDIRECT_URI = "http://localhost:5173/oauth/callback";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String authCode = (String) oAuth2User.getAttributes().get("authCode");
        log.info("[onAuthenticationSuccess] 인증 코드: {}", authCode);

        if (authCode == null) {
            log.warn("[onAuthenticationSuccess] 인증 코드가 누락되었습니다.");
            httpServletResponse.sendRedirect(REDIRECT_URI + "?error=auth_code_missing");
            return;
        }

        // authCode와 함께 프론트엔드로 리다이렉트
        String redirectUrl = REDIRECT_URI + "?authCode=" + authCode;
        log.info("[onAuthenticationSuccess] 리다이렉트 URL: {}", redirectUrl);
        httpServletResponse.sendRedirect(redirectUrl);
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
