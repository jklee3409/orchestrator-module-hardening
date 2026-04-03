package eureca.capstone.project.orchestrator.auth.util;

import eureca.capstone.project.orchestrator.common.config.properties.AppCookieProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import static eureca.capstone.project.orchestrator.auth.constant.TokenConstant.REFRESH_TOKEN_COOKIE_NAME;
import static eureca.capstone.project.orchestrator.auth.constant.TokenConstant.REFRESH_TOKEN_MAX_AGE_SEC;

@Slf4j
@Component
@RequiredArgsConstructor
public class CookieUtil {
    private final AppCookieProperties cookieProperties;

    public void createRefreshTokenCookie(String refreshToken, HttpServletResponse httpServletResponse) {
        log.info("[createRefreshTokenCookie] creating refresh token cookie");

        ResponseCookie.ResponseCookieBuilder refreshCookieBuilder = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(cookieProperties.secure())
                .path("/")
                .maxAge(REFRESH_TOKEN_MAX_AGE_SEC)
                .sameSite(cookieProperties.sameSite());

        if (StringUtils.hasText(cookieProperties.domain())) {
            refreshCookieBuilder.domain(cookieProperties.domain());
        }

        ResponseCookie refreshCookie = refreshCookieBuilder.build();
        httpServletResponse.addHeader("Set-Cookie", refreshCookie.toString());
    }

    public String extractTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            log.info("request.getCookies() is null");
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(REFRESH_TOKEN_COOKIE_NAME)) {
                log.info("cookie.getName : {}", REFRESH_TOKEN_COOKIE_NAME);
                return cookie.getValue();
            }
        }
        return null;
    }
}
