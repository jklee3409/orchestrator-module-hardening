# common 도메인 인덱스

## 역할

`common`은 shared configuration, base entity, response envelope, exception, type manager, Redis access, email, AI, utility service를 제공한다.

## 구조

- `config`: Redis, QueryDSL, Swagger, WebClient, AI, typed property 설정
- `component`: initialization 및 shared selection component
- `constant`: shared constant
- `controller`: shared operational endpoint
- `dto`: base response 및 shared DTO
- `entity`: base 및 lookup entity
- `exception`: global handler, error code, custom exception
- `repository`: shared lookup repository
- `service`: Redis, email, verification, AI service
- `util`: cached type manager 및 shared utility accessor

## 핵심 진입점

- `GlobalExceptionHandler`
- `ErrorCode`
- `StatusManager`
- `SalesTypeManager`
- `PayTypeManager`
- `ChangeTypeManager`
- `AlarmTypeManager`
- `RedisService`

## 에이전트 규칙

- 이 도메인을 수정하기 전에 이 파일과 `progress.md`를 읽어야 한다.
- 이 도메인의 변경은 cross-domain change로 취급해야 한다.
- 최소 두 개 이상의 도메인이 같은 동작을 필요로 하기 전에는 shared abstraction을 추가하면 안 된다.
- 이 도메인 하위 변경 후 `progress.md`를 갱신해야 한다.
