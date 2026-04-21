# Architecture 규칙

## 필수 규칙

- Controller는 request mapping, 인증 principal 추출, DTO binding, response wrapping만 처리해야 한다.
- Service는 domain rule, transaction boundary, orchestration, event publishing을 소유해야 한다.
- Repository는 persistence query만 소유해야 한다.
- package가 request, response, common DTO 구조를 이미 따르는 경우 그 분리를 유지해야 한다.
- transaction 이후 side effect에는 domain event를 사용해야 한다.
- cached enum-like data는 `StatusManager`, `SalesTypeManager`, `PayTypeManager`, `ChangeTypeManager`, `AlarmTypeManager`를 재사용해야 한다.

## 금지 규칙

- Controller에서 Repository를 직접 호출하면 안 된다.
- Controller에서 Pay update, Redis script, Kafka send, Elasticsearch update를 실행하면 안 된다.
- Repository implementation에 business decision을 넣으면 안 된다.
- JPA entity를 API response로 직접 노출하면 안 된다.
- 임시 static helper로 service boundary를 우회하면 안 된다.
- 명시적 요청 없이 package 이동, public class rename, 대규모 abstraction 추가를 하면 안 된다.

## 검증

- architecture 영향이 있는 변경은 변경된 call path가 controller -> service -> repository 방향을 지키는지 확인해야 한다.
- event 변경은 해당 side effect가 persisted state 자체가 아닌 한 main DB transaction 밖에서 실행되는지 확인해야 한다.
