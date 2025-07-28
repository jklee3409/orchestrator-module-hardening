package eureca.capstone.project.orchestrator.auth.service.impl;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.auth.service.TokenService;
import eureca.capstone.project.orchestrator.auth.util.CookieUtil;
import eureca.capstone.project.orchestrator.auth.util.JwtUtil;
import eureca.capstone.project.orchestrator.common.exception.custom.RefreshTokenMismatchException;
import eureca.capstone.project.orchestrator.common.service.RedisService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

import static eureca.capstone.project.orchestrator.common.constant.RedisConstant.REDIS_REFRESH_TOKEN;

;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final RedisService redisService;

    @Override
    public String generateToken(CustomUserDetailsDto customUserDetailsDto, HttpServletResponse httpServletResponse) {
        // 요청 값 로그 출력
        log.info("Generating token - customUserDetailsDto : {}", customUserDetailsDto);
        log.info("Generating token - httpServletResponse : {}", httpServletResponse);

        // 값 추출
        String email = customUserDetailsDto.getEmail();
        Long userId = customUserDetailsDto.getUserId();
        Set<String> roles = customUserDetailsDto.getRoleStrings();
        Set<String> authorities = customUserDetailsDto.getAuthorityStrings();

        // JWT 토큰 발급
        String accessToken = jwtUtil.generateAccessToken(email, roles, authorities, userId);
        String refreshToken = jwtUtil.generateRefreshToken(email, roles, authorities, userId);
        log.info("Generated access token - accessToken: {}", accessToken);
        log.info("Generated access token - refreshToken: {}", refreshToken);

        // Refresh 토큰 Response 헤더에 할당 및 레디스에 저장 (14일 보관)
        cookieUtil.createRefreshTokenCookie(refreshToken, httpServletResponse);
        redisService.setValue(REDIS_REFRESH_TOKEN + userId, refreshToken, Duration.ofDays(14));

        // return
        return accessToken;
    }

    @Override
    public String reGenerateToken(CustomUserDetailsDto customUserDetailsDto, HttpServletResponse httpServletResponse) {
        // 요청 값 로그 출력
        log.info("reGenerateToken token - customUserDetailsDto : {}", customUserDetailsDto);
        log.info("reGenerateToken token - httpServletResponse : {}", httpServletResponse);
        // 서버에서 발급한 리프레쉬 토큰값이 맞는지 확인
        if (!redisService.hasKey(REDIS_REFRESH_TOKEN + customUserDetailsDto.getUserId())) {
            log.error("redis 에 해당 리프레쉬 토큰 값이 존재하지 않습니다.");
            throw new RefreshTokenMismatchException();
        }
        // 토큰 재 발행
        return generateToken(customUserDetailsDto, httpServletResponse);
    }
}
