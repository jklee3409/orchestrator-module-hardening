# Bid Pipeline 규칙

## 필수 규칙

- bid success는 committed DB state로 판단해야 한다.
- Redis Lua success는 concurrency prewrite로만 처리해야 한다.
- DB confirmation은 bid 확정 전에 lock 상태로 feed를 다시 읽어야 한다.
- DB confirmation 실패 시 state version 기반 conditional Redis rollback을 실행해야 한다.
- 이전 최고 입찰자 환불은 DB highest bid state 기준으로 계산해야 한다.
- 신규 입찰자 Pay 차감은 bid history 저장과 같은 transactional flow 안에서 저장해야 한다.
- `BidSucceededEvent`는 bid가 commit 가능한 상태가 된 뒤에만 발행해야 한다.
- Elasticsearch 및 alarm side effect는 commit 이후 `BidSucceededEventHandler`에서 실행해야 한다.

## 금지 규칙

- bid precondition validation을 생략하면 안 된다.
- DB lock confirmation을 생략하면 안 된다.
- Redis highest price를 Pay 환불 또는 차감의 최종 기준으로 사용하면 안 된다.
- `bid.lua` 또는 `bid_rollback.lua`를 임시 Java-only concurrency logic으로 대체하면 안 된다.
- transaction commit 전에 Kafka notification을 발행하면 안 된다.
- transaction commit 전에 `TransactionFeedDocument.currentHighestPrice`를 갱신하면 안 된다.

## 필수 확인 파일

- `src/main/java/eureca/capstone/project/orchestrator/transaction_feed/service/impl/BidServiceImpl.java`
- `src/main/resources/scripts/bid.lua`
- `src/main/resources/scripts/bid_rollback.lua`
- `src/main/java/eureca/capstone/project/orchestrator/transaction_feed/event/BidSucceededEventHandler.java`
- bid flow에서 사용하는 관련 Pay service method

## 검증

- bid logic 변경 시 focused bid service test를 실행해야 한다.
- DB confirmation 실패 가능성이 있는 변경은 Redis rollback 동작을 검증해야 한다.
- Pay 환불 및 차감 금액이 DB bid state와 일치하는지 검증해야 한다.
