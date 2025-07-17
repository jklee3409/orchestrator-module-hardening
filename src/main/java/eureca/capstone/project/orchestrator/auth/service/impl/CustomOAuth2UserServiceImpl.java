package eureca.capstone.project.orchestrator.auth.service.impl;

import eureca.capstone.project.orchestrator.user.service.UserService;
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

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("[loadUser] userRequest: {}", userRequest);
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // SNS 고유 ID 변수 (카카오, 구글, 애플)
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
        }

        // TODO 차단된 사용자인지 검증 필요

        // 시스템에 등록되지 않은 OAuth 사용자 확인 및 등록 + 핸들러에서 사용할 userId, email 담기
        Long userId = userService.OAuthUserRegisterIfNotExists(email, provider);
        log.info("[loadUser] userId -> {}", userId);
        Map<String, Object> deepCopyAttributes = new HashMap<>(attributes);
        deepCopyAttributes.put("userId", userId);
        deepCopyAttributes.put("email", email);

        // return
        return new DefaultOAuth2User(
                Set.of(new SimpleGrantedAuthority("ROLE_USER")),
                deepCopyAttributes,
                userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName()
        );
    }
}
