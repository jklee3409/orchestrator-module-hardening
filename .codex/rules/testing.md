# Testing 규칙

## 필수 규칙

- authentication, bidding, Pay, transaction history, event, external projection 동작이 변경되면 test를 추가하거나 갱신해야 한다.
- dependency를 mock 처리할 수 있는 변경은 변경 class 근처에 focused unit test를 유지해야 한다.
- persistence, lock behavior, QueryDSL, Testcontainers behavior가 변경 표면이면 integration test를 사용해야 한다.
- validation, Pay, bid rollback, authentication 변경은 happy path뿐 아니라 failure path도 검증해야 한다.
- 최종 응답에 실행한 모든 test command를 명시해야 한다.

## 금지 규칙

- build를 통과시키기 위해 test를 삭제하면 안 된다.
- product contract가 명시적으로 바뀌지 않은 상태에서 assertion을 약화하면 안 된다.
- 변경 동작 검증을 생략하면서 이유를 명시하지 않으면 안 된다.
- deterministic coordination이 가능한 concurrency test에 brittle sleep을 추가하면 안 된다.
- 안정적인 behavior contract를 검증할 수 있는데 implementation detail만 assert하면 안 된다.

## 검증 명령

- Focused test: `.\gradlew.bat test --tests "<fully.qualified.TestClass>"`
- Full test suite: `.\gradlew.bat test`
- Build: `.\gradlew.bat build`
