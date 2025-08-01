package eureca.capstone.project.orchestrator.auth.controller;

import eureca.capstone.project.orchestrator.auth.dto.response.LoginResponseDto;
import eureca.capstone.project.orchestrator.auth.entity.UserAuthority;
import eureca.capstone.project.orchestrator.auth.repository.UserAuthorityRepository;
import eureca.capstone.project.orchestrator.auth.util.CookieUtil;
import eureca.capstone.project.orchestrator.auth.util.JwtUtil;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.common.service.RedisService;
import eureca.capstone.project.orchestrator.user.dto.UserInformationDto;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static eureca.capstone.project.orchestrator.common.constant.RedisConstant.REDIS_BLACK_LIST_USER;
import static eureca.capstone.project.orchestrator.common.constant.RedisConstant.REDIS_REFRESH_TOKEN;

@Tag(name = "OAuth API", description = "소셜 로그인 및 토큰 발급 API")
@Slf4j
@RestController
@RequestMapping("/orchestrator/oauth")
@RequiredArgsConstructor
public class OAuthController {
    private final RedisService redisService;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final UserRepository userRepository;
    private final UserAuthorityRepository userAuthorityRepository;

    @PostMapping("/token")
    @Operation(summary = "인증 코드로 JWT 토큰 교환", description = """
            ## 소셜 로그인 성공 후 발급받은 임시 인증 코드를 JWT(Access Token, Refresh Token)로 교환합니다.
            
            ### API 호출 흐름
            1. 클라이언트가 소셜 로그인(카카오, 구글 등)을 진행합니다.
            2. 로그인 성공 시, 서버는 클라이언트로 리다이렉트하면서 임시 `authCode`를 발급합니다.
            3. 클라이언트는 이 API로 `authCode`를 전송하여 최종 토큰 발급을 요청합니다.
            4. 서버는 `authCode`를 검증한 후, `Access Token`을 응답 본문에, `Refresh Token`을 `HttpOnly` 쿠키에 담아 반환합니다.
            
            ***
            
            ### 📥 요청 바디 (Request Body)
            ```json
            {
              "authCode": "a1b2c3d4-e5f6-1234-abcd-e9f8g7h6i5j4"
            }
            ```
            
            ### 📥 요청 바디 필드 설명
            * `authCode`: 소셜 로그인 성공 후 서버로부터 발급받은 임시 인증 코드 (UUID 형식)
            
            ### ✅ 성공 응답 (Success Response)
            * **Response Body**:
                ```json
                {
                  "statusCode": 200,
                  "message": "success",
                  "data": {
                    "accessToken": "ey...",
                    "isNewUser": true
                  }
                }
                ```
            * **Response Cookie**: `refreshToken`이 `HttpOnly`, `Secure`, `SameSite=None` 쿠키로 설정되어 반환됩니다.
            
            ### 🔑 권한
            * 없음 (유효한 `authCode`만 필요)
            
            ### ❌ 주요 실패 케이스
            * **HTTP 401 Unauthorized**: 유효하지 않거나 만료된 `authCode`를 보냈을 경우 (이 API는 표준 에러 포맷을 따르지 않음)
            """)
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

        // 혹시 제제 대상인 사용자라면, 이미 로그인시에 필요한 제제권한을 제외한 권한을 갖게 되므로, 예외를 터트려서 로그인 페이지로 이동할 필요가 없음
        // 해서, 해당 레디스 값을 삭제 처리함.
        if (redisService.hasKey(REDIS_BLACK_LIST_USER + userId)) {
            redisService.deleteValue(REDIS_BLACK_LIST_USER + userId);
            log.error("[JwtFilter] Blacklisted user delete key. key: {}", REDIS_BLACK_LIST_USER + userId);
        }

        // 제재 당한 권한이 있는지 확인 및 기존 권한에서 제외 및 로그 출력
        List<UserAuthority> blockUserList = userAuthorityRepository
                .findUserAuthorityByUserId(userInformationDto.getUserId());
        Set<String> blockUserAuthority = blockUserList.stream()
                .map(blockUser -> blockUser.getAuthority().getName())
                .collect(Collectors.toSet());
        authorities.removeAll(blockUserAuthority);
        log.info("blockUserList : {}", blockUserList);
        log.info("blockUserAuthority : {}", blockUserAuthority);
        log.info("authorities : {}", authorities);

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
