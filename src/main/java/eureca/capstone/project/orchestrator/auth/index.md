# auth 도메인 인덱스

## 역할

`auth`는 JWT authentication, OAuth login, token reissue, security filter, role, authority를 담당한다.

## 구조

- `controller`: auth, OAuth, token endpoint
- `service`: token, OAuth, user details, OAuth success flow
- `filter`: JWT authentication filter 및 benchmark bypass gate
- `config`: Spring Security 및 password encoder 설정
- `repository`: role, authority, mapping query
- `entity`: role 및 authority mapping entity
- `dto`: login, token, OAuth, user details DTO
- `util`: JWT 및 cookie utility

## 핵심 진입점

- `SecurityConfig`
- `JwtAuthenticationFilter`
- `TokenServiceImpl`
- `CustomUserDetailsServiceImpl`
- `CustomOAuth2UserServiceImpl`
- `CustomOAuth2SuccessServiceImpl`

## 에이전트 규칙

- 이 도메인을 수정하기 전에 이 파일과 `progress.md`를 읽어야 한다.
- auth, filter, OAuth, JWT, profile security 변경 시 `.codex/rules/security.md`를 읽어야 한다.
- JWT, role, authority, blacklist, token validation을 약화하면 안 된다.
- 이 도메인 하위 변경 후 `progress.md`를 갱신해야 한다.
