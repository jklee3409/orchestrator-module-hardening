package eureca.capstone.project.orchestrator.auth.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import eureca.capstone.project.orchestrator.auth.util.CookieUtil;
import eureca.capstone.project.orchestrator.auth.util.JwtUtil;
import eureca.capstone.project.orchestrator.common.config.properties.AppUrlProperties;
import eureca.capstone.project.orchestrator.common.service.RedisService;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2SuccessServiceImpl implements AuthenticationSuccessHandler {
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final UserRepository userRepository;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final AppUrlProperties appUrlProperties;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse,
            Authentication authentication
    ) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String authCode = (String) oAuth2User.getAttributes().get("authCode");
        log.info("[onAuthenticationSuccess] authCode={}", authCode);

        if (authCode == null) {
            httpServletResponse.sendRedirect(appUrlProperties.oauthSuccessRedirect() + "?error=auth_code_missing");
            return;
        }

        HttpSession session = httpServletRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        httpServletResponse.sendRedirect(appUrlProperties.oauthSuccessRedirect() + "?authCode=" + authCode);
    }

    public void writeJsonResponse(HttpServletResponse httpServletResponse, Object dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            httpServletResponse.setContentType("application/json");
            httpServletResponse.setCharacterEncoding("UTF-8");
            httpServletResponse.getWriter().write(json);
        } catch (Exception e) {
            throw new RuntimeException("JSON response write failed", e);
        }
    }
}
