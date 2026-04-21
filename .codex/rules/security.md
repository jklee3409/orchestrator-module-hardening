# Security 규칙

## 필수 규칙

- JWT validation은 기본 authentication path로 유지해야 한다.
- OAuth success handling은 provider-specific identity mapping을 명시적으로 유지해야 한다.
- JMeter bypass는 `security.jmeter-bypass.enabled=true`와 일치하는 `X-JMeter-Test-Key`가 있을 때만 동작해야 한다.
- JMeter bypass는 benchmark asset이 사용하는 configured header/token shape로만 인증해야 한다.
- local-only bypass default는 production profile에 들어가면 안 된다.
- secret은 environment variable 또는 secure runtime configuration에서 읽어야 한다.

## 금지 규칙

- JWT signature, expiry, blacklist, role, authority validation을 약화하면 안 된다.
- global authentication bypass를 추가하면 안 된다.
- production에서 JMeter bypass가 기본 활성화되게 만들면 안 된다.
- raw secret, token, refresh token, payment key, OAuth credential을 log로 남기면 안 된다.
- authentication 실패 시 fallback user 또는 fallback authority를 도입하면 안 된다.
- `.env`, private key, real external API secret을 commit하면 안 된다.

## 검증

- filter 또는 security config 변경 시 focused filter/authentication test를 실행해야 한다.
- `application-prod.properties`에서 bypass가 disabled이고 test key 기본값이 없는지 확인해야 한다.
