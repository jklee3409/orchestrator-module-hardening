package eureca.capstone.project.orchestrator.auth.service.impl;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.auth.dto.request.LoginRequestDto;
import eureca.capstone.project.orchestrator.auth.service.TokenService;
import eureca.capstone.project.orchestrator.auth.util.CookieUtil;
import eureca.capstone.project.orchestrator.auth.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;

    @Override
    public String generateToken(LoginRequestDto loginRequestDto, CustomUserDetailsDto customUserDetailsDto, HttpServletResponse httpServletResponse) {
        log.info("Generating token - loginRequestDto : {}", loginRequestDto);
        log.info("Generating token - customUserDetailsDto : {}", customUserDetailsDto);
        log.info("Generating token - httpServletResponse : {}", httpServletResponse);

        String email = customUserDetailsDto.getEmail();
        Long userId = customUserDetailsDto.getUserId();
        Set<String> roles = customUserDetailsDto.getRoleStrings();
        Set<String> authorities = customUserDetailsDto.getAuthorityStrings();

        String accessToken = jwtUtil.generateAccessToken(email, roles, authorities, userId);
        String refreshToken = jwtUtil.generateAccessToken(email, roles, authorities, userId);
        log.info("Generated access token - accessToken: {}", accessToken);
        log.info("Generated access token - refreshToken: {}", refreshToken);

        cookieUtil.createRefreshTokenCookie(refreshToken, httpServletResponse);

        return accessToken;
    }
}
