package eureca.capstone.project.orchestrator.auth.config;

import eureca.capstone.project.orchestrator.auth.filter.JwtAuthenticationFilter;
import eureca.capstone.project.orchestrator.auth.service.impl.CustomOAuth2SuccessServiceImpl;
import eureca.capstone.project.orchestrator.auth.service.impl.CustomOAuth2UserServiceImpl;
import eureca.capstone.project.orchestrator.auth.util.CookieUtil;
import eureca.capstone.project.orchestrator.auth.util.JwtUtil;
import eureca.capstone.project.orchestrator.common.config.properties.JmeterBypassProperties;
import eureca.capstone.project.orchestrator.common.service.RedisService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static eureca.capstone.project.orchestrator.auth.constant.FilterConstant.*;

@Slf4j
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final RedisService redisService;
    private final UserDetailsService userDetailsService;
    private final CustomOAuth2UserServiceImpl customOAuth2UserService;
    private final CustomOAuth2SuccessServiceImpl customOAuth2SuccessService;
    private final JmeterBypassProperties jmeterBypassProperties;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 보호 비활성화 (JWT 기반이므로 필요 없음)
                .csrf(AbstractHttpConfigurer::disable)

                // CORS 기본 설정 적용 (필요 시 커스터마이징 가능)
                .cors(Customizer.withDefaults())

                // 폼 로그인 비활성화
                .formLogin(AbstractHttpConfigurer::disable)

                // HTTP Basic 인증도 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)

                // 세션을 생성하지 않고, 완전한 무상태(stateless) 방식 사용
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                // 요청별 인증/인가 정책 정의
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(WHITE_LIST).permitAll()                // 화이트리스트 경로는 모두 허용
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_URIS).permitAll()
                        .requestMatchers(BLACK_LIST).authenticated()           // 블랙리스트 경로는 인증 필요
                        .anyRequest().permitAll()                              // 나머지는 모두 허용 (필요 시 변경)
                )

                // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 전에 등록
                .addFilterBefore(
                        new JwtAuthenticationFilter(
                                jwtUtil,
                                cookieUtil,
                                redisService,
                                userDetailsService,
                                jmeterBypassProperties
                        ),
                        UsernamePasswordAuthenticationFilter.class
                )

                // OAuth 2.0 로그인 설정
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(user -> user
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(customOAuth2SuccessService)
                )

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            log.warn("[인증 실패] URI: {}, 원인: {}", request.getRequestURI(), authException.getMessage());
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.setContentType("application/json");
                            String unauthorizedJson = """
                                    {
                                      "statusCode": 401,
                                      "message": "fail",
                                      "data": "false"
                                    }
                                    """;
                            response.getWriter().write(unauthorizedJson);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            log.warn("[인가 실패] URI: {}, 원인: {}", request.getRequestURI(), accessDeniedException.getMessage());
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.setContentType("application/json");
                            String scForbiddenJson = """
                                    {
                                      "statusCode": 403,
                                      "message": "fail",
                                      "data": "false"
                                    }
                                    """;
                            response.getWriter().write(scForbiddenJson);
                        })
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}

