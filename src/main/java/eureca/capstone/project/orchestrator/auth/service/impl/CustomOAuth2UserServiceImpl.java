package eureca.capstone.project.orchestrator.auth.service.impl;

import eureca.capstone.project.orchestrator.auth.dto.OAuthRegistrationResultDto;
import eureca.capstone.project.orchestrator.common.service.RedisService;
import eureca.capstone.project.orchestrator.user.service.UserService;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserServiceImpl implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final UserService userService;
    private final RedisService redisService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("[loadUser] userRequest: {}", userRequest);
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // SNS 고유 ID 변수 (카카오, 구글, 네이버)
        String providerId = null;
        String email = null;

        switch (provider) {
            case "kakao" -> {
                // 카카오는 id 고유 식별자
                providerId = String.valueOf(attributes.get("id"));
                email = providerId + "@kakao.com";
                log.info("[loadUser] kakao: providerId -> {} , email -> {}", providerId, email);
            }
            case "google" -> {
                // 구글은 sub 고유 식별자
                providerId = (String) attributes.get("sub");
                email = providerId + "@gmail.com";
                log.info("[loadUser] google: providerId -> {} , email -> {}", providerId, email);
            }
            case "naver" -> {
                // 네이버 response 고유 식별자
                Map<String, Object> response = (Map<String, Object>) attributes.get("response");
                providerId = (String) response.get("id");  // 네이버 고유 식별자
                email = providerId + "@naver.com";
                log.info("[loadUser] naver: providerId -> {}, email -> {}", providerId, email);
            }
        }

        // 사용자가 존재하는지 확인하고 등록 결과(userId, isNewUser)를 가져옴
        OAuthRegistrationResultDto registrationResult = userService.OAuthUserRegisterIfNotExists(email, provider);
        log.info("[loadUser] userId -> {}, isNewUser -> {}", registrationResult.getUserId(), registrationResult.isNewUser());

        // 임시 인증 코드를 생성하여 redis 에 저장
        String authCode = UUID.randomUUID().toString();
        Map<String, Object> redisPayload = new HashMap<>();
        redisPayload.put("email", email);
        redisPayload.put("isNewUser", registrationResult.isNewUser());
        redisService.setValue("oauth-temp-token:" + authCode, redisPayload, Duration.ofMinutes(5)); // 5분 만료

        // 성공 핸들러에 authCode를 전달하기 위해 속성에 추가
        Map<String, Object> deepCopyAttributes = new HashMap<>(attributes);
        deepCopyAttributes.put("authCode", authCode);

        // return
        return new DefaultOAuth2User(
                Set.of(new SimpleGrantedAuthority("ROLE_USER")),
                deepCopyAttributes,
                userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName()
        );
    }
}
