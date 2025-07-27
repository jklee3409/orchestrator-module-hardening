package eureca.capstone.project.orchestrator.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import eureca.capstone.project.orchestrator.auth.constant.FilterConstant;
import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.auth.util.CookieUtil;
import eureca.capstone.project.orchestrator.auth.util.JwtUtil;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.common.dto.base.ErrorResponseDto;
import eureca.capstone.project.orchestrator.common.exception.code.ErrorCode;
import eureca.capstone.project.orchestrator.common.exception.custom.BlackListUserException;
import eureca.capstone.project.orchestrator.common.service.RedisService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static eureca.capstone.project.orchestrator.common.constant.RedisConstant.REDIS_BLACK_LIST_USER;


@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final RedisService redisService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        if (!requestURI.equals("/healthCheck")) {
            log.info("[JwtFilter] Incoming request URI: {}", requestURI);
        }

        String authHeader;
        if (requestURI.equals(FilterConstant.REFRESH_PATH)) {
            String refreshTokenByCookie = cookieUtil.extractTokenFromCookie(request);
            log.info("[JwtFilter] Refresh token by cookie is: {}", refreshTokenByCookie);
            authHeader = "Bearer " + refreshTokenByCookie;
        } else {
            authHeader = request.getHeader("Authorization");
        }

        // 토큰이 없는 경우
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // 공개된 경로(Pass-listed)는 인증 없이 통과
            if (isPassListed(request)) {
                log.debug("[JwtFilter] No token found for pass-listed URI: {}. Passing as anonymous.", requestURI);
                chain.doFilter(request, response);
                return;
            }
            // 보호된 경로인데 토큰이 없으면 에러 반환
            log.warn("Authorization header missing or invalid for protected URI: {}", requestURI);
            writeErrorResponse(response, ErrorCode.MISSING_TOKEN);
            return;
        }

        // 토큰이 있는 경우, 경로와 상관없이 검증 시도
        String token = authHeader.substring(7);
        try {
            jwtUtil.isValidToken(token);

            Long userId = jwtUtil.extractUserId(token);
            String email = jwtUtil.extractEmail(token);
            Set<String> roles = jwtUtil.extractRoles(token);
            Set<String> authorities = jwtUtil.extractAuthorities(token);

            if (redisService.hasKey(REDIS_BLACK_LIST_USER + userId)) {
                log.error("[JwtFilter] Blacklisted user access attempt. User ID: {}", userId);
                throw new BlackListUserException();
            }

            List<SimpleGrantedAuthority> grantedAuthorities = new ArrayList<>();
            for (String role : roles) grantedAuthorities.add(new SimpleGrantedAuthority(role));
            for (String authority : authorities) grantedAuthorities.add(new SimpleGrantedAuthority(authority));

            CustomUserDetailsDto userDetails = new CustomUserDetailsDto(userId, email, "", grantedAuthorities);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("[JwtFilter] Authentication successful for user: {}. URI: {}", email, requestURI);

        } catch (ExpiredJwtException e) {
            log.warn("[JwtFilter] Expired JWT token for URI: {}. Message: {}", requestURI, e.getMessage());
            writeErrorResponse(response, ErrorCode.TOKEN_EXPIRED);
            return;
        } catch (SignatureException e) {
            log.warn("[JwtFilter] Invalid JWT signature for URI: {}. Message: {}", requestURI, e.getMessage());
            writeErrorResponse(response, ErrorCode.INVALID_SIGNATURE);
            return;
        } catch (MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            log.warn("[JwtFilter] Malformed/Unsupported/Illegal JWT token for URI: {}. Message: {}", requestURI, e.getMessage());
            writeErrorResponse(response, ErrorCode.MALFORMED_TOKEN);
            return;
        } catch (BlackListUserException e) {
            writeErrorResponse(response, ErrorCode.BLACK_LIST_USER_FOUND);
            return;
        }

        chain.doFilter(request, response);
    }

    private void writeErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        log.info("ErrorCode : {}", errorCode);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        BaseResponseDto<ErrorResponseDto> fail = BaseResponseDto.fail(errorCode);
        log.info("fail : {}", fail);

        String json = new ObjectMapper().writeValueAsString(fail);
        response.getWriter().write(json);
    }

    private boolean isPassListed(HttpServletRequest request) {
        String uri = request.getRequestURI();

        // WHITE_LIST에 있는 경로는 HTTP 메서드와 상관없이 항상 통과
        for (String path : FilterConstant.WHITE_LIST) {
            if (pathMatcher.match(path, uri)) {
                return true;
            }
        }

        // PUBLIC_GET_URIS는 GET 메서드일 경우에만 통과
        if (request.getMethod().equalsIgnoreCase("GET")) {
            for (String path : FilterConstant.PUBLIC_GET_URIS) {
                if (pathMatcher.match(path, uri)) {
                    return true;
                }
            }
        }

        return false;
    }
}
