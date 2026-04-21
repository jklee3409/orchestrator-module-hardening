# Data Integrity 규칙

## 필수 규칙

- user, feed, bid, Pay, history, alarm, status data의 source of truth는 MySQL이어야 한다.
- Redis는 session, cache, bid coordination, Pub/Sub delivery 용도로만 사용해야 한다.
- Elasticsearch는 committed feed state의 searchable projection으로만 사용해야 한다.
- Kafka는 asynchronous alarm creation 및 delivery flow 용도로만 사용해야 한다.
- Redis bid key는 다음 naming scheme을 유지해야 한다.
  - `bids:{feedId}:highest_price`
  - `bids:{feedId}:highest_bidder_id`
  - `bids:{feedId}:state_version`
- `build/generated/` 하위 QueryDSL generated file은 직접 수정하지 말고 재생성해야 한다.

## 금지 규칙

- `transaction_feed`, `bids`, `pay`, `user`, `alarm`, history table을 통합하면 안 된다.
- 명시적 승인 없이 기존 column 의미를 재정의하면 안 된다.
- transactional correctness가 필요한 MySQL read를 Elasticsearch로 대체하면 안 된다.
- Kafka message를 transaction confirmation으로 사용하면 안 된다.
- Redis key name 변경 시 script, service, test, benchmark asset을 함께 갱신하지 않으면 안 된다.
- generated build artifact를 commit하면 안 된다.

## 검증

- schema 또는 entity 변경 시 repository query와 DTO mapping을 확인해야 한다.
- Redis key 변경 시 Lua script와 Java key construction을 함께 확인해야 한다.
- Elasticsearch projection 변경 시 committed DB state에서 projection이 재구성되는지 확인해야 한다.
