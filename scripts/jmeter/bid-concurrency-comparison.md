# 입찰 동시성 부하 테스트 비교 시나리오

## 목적

입찰 API의 동시성 제어 방식에 따른 처리 성능과 안정성을 비교한다.

- Redis 선검증 방식: `POST /orchestrator/bid`
- DB Lock 선점 방식: `POST /orchestrator/bid/db-lock`

현재 두 API는 후속 처리 흐름을 동일하게 사용한다.

- DB 기준 최고 입찰 검증
- 이전 최고 입찰자 환불
- 신규 입찰자 페이 차감
- 입찰 이력 저장
- `BidSucceededEvent` 발행
- AFTER_COMMIT 이후 알림 발송 및 Elasticsearch 반영

비교 포인트는 후속 처리 로직이 아니라, "최고가 비교 및 경쟁 제어를 어디서 먼저 수행하느냐"에 있다.

## 테스트 전제

- 동일한 판매글에 대해 다수 사용자가 동시에 입찰한다.
- 두 시나리오는 같은 사용자 수, 같은 램프업, 같은 입찰 금액 데이터셋으로 실행한다.
- 성능 비교 전후로 DB 상태와 사용자 잔액은 초기화한다.

## JMeter 인증 방식

`JwtAuthenticationFilter`에는 JMeter 부하 테스트용 인증 우회 로직이 이미 포함되어 있다.

요청 헤더는 아래와 같이 설정한다.

- `X-JMeter-Test-Key: URECA-TEST-SECRET-KEY-!@#$`
- `Authorization: Bearer ${user_email}`
- `Content-Type: application/json`

주의:

- 우회 모드에서는 JWT를 넣는 것이 아니라, `Bearer ` 뒤 값을 사용자 이메일로 해석한다.
- 따라서 CSV에 들어가는 `user_email`은 실제 DB에 존재하는 사용자 이메일이어야 한다.

## 요청 바디 형식

```json
{
  "transactionFeedId": ${feed_id},
  "bidAmount": ${bid_amount}
}
```

## JMeter 구성 권장안

### 공통 구성

- `Thread Group`
- `CSV Data Set Config`
- `HTTP Request Defaults`
- `HTTP Header Manager`
- `Synchronizing Timer`
- `Summary Report`
- `Aggregate Report`
- `Response Time Percentiles`

참고:

- `View Results Tree`는 디버깅 단계에서만 사용하고, 본 부하 테스트에서는 비활성화하는 것을 권장한다.
- 동시 시작 효과를 높이려면 `Synchronizing Timer`를 HTTP Request 바로 앞에 둔다.

### CSV 예시

```text
user_email,feed_id,bid_amount
bidder1@test.com,100,10100
bidder2@test.com,100,10200
bidder3@test.com,100,10300
...
```

권장 사항:

- 사용자 이메일은 모두 서로 달라야 한다.
- `feed_id`는 비교 대상 시나리오에서 동일해야 한다.
- `bid_amount`는 100원 단위여야 하며, 가급적 오름차순으로 구성하는 편이 결과 해석이 쉽다.

## 시나리오 1. Redis 선검증 방식

- 대상 API: `POST /orchestrator/bid`
- 목적: Redis에서 최고 입찰 비교 및 갱신을 먼저 수행하는 현재 운영 방식의 기준 성능 측정

예상 특성:

- 경쟁이 높은 상황에서 DB 락 경합을 상대적으로 덜 유발할 가능성이 있다.
- 대신 Redis와 DB 상태 차이를 보정하는 후속 검증 흐름이 포함된다.

## 시나리오 2. DB Lock 선점 방식

- 대상 API: `POST /orchestrator/bid/db-lock`
- 목적: 트랜잭션 시작 시점에 DB 비관적 락을 선점하는 비교용 방식의 성능 측정

예상 특성:

- 처리 흐름은 단순하지만, 동시 요청 수가 높아질수록 락 대기 시간이 커질 수 있다.
- 고경합 상황에서 평균 응답시간과 상위 퍼센타일 지연이 더 커질 가능성이 있다.

## 실행 절차

1. 비교 대상 판매글 1건을 준비한다.
2. 입찰 대상 사용자 계정과 페이 잔액을 충분히 세팅한다.
3. Redis 방식 API로 1차 실행한다.
4. DB, Redis, 사용자 잔액, 입찰 이력을 초기화한다.
5. 동일한 CSV와 동일한 JMeter 설정으로 DB Lock 방식 API를 2차 실행한다.
6. 두 결과를 같은 기준으로 비교한다.

## 1차 권장 부하 구간

- 20 users / ramp-up 1s / loop 1
- 50 users / ramp-up 1s / loop 1
- 100 users / ramp-up 1s / loop 1

필요 시 아래 순서로 확장한다.

- 200 users / ramp-up 1s / loop 1
- 500 users / ramp-up 1s / loop 1

실무 팁:

- 처음부터 큰 부하로 가지 말고, 작은 구간에서 오류 패턴과 데이터 정합성을 먼저 확인한 뒤 확대하는 편이 안전하다.
- 응답시간만 보지 말고 실패 건의 성격도 함께 확인해야 한다.

## 비교 지표

아래 항목을 최소 비교 지표로 사용한다.

- Throughput
- Average Response Time
- p95 Response Time
- p99 Response Time
- Error Count
- 성공 요청 수 대비 실제 DB 반영 건수

추가로 보면 좋은 항목:

- 락 대기 때문에 발생한 응답시간 증가 패턴
- 특정 구간에서 오류가 집중되는지 여부
- 재실행 시 결과 편차가 큰지 여부

## 결과 확인 체크리스트

- 최종 DB 최고 입찰가가 기대한 최대 입찰 금액과 일치하는가
- 이전 최고 입찰자의 페이가 정상 환불되었는가
- 최종 낙찰 입찰자의 페이가 1회만 차감되었는가
- 성공한 입찰만 이력으로 저장되었는가
- AFTER_COMMIT 이후 알림과 Elasticsearch 반영이 정상 수행되었는가
- 두 방식 모두 데이터 정합성 측면에서 동일한 결과를 보장하는가

## 해석 가이드

- `/orchestrator/bid`가 더 높은 처리량과 낮은 지연을 보이면, Redis 선검증 방식이 고경합 입찰에 더 적합하다고 판단할 수 있다.
- `/orchestrator/bid/db-lock`의 지연이 빠르게 증가하면, 락 경합이 병목으로 작용하고 있을 가능성이 높다.
- 반대로 성능 차이가 크지 않다면, 운영 복잡도와 장애 대응 난이도까지 포함해 구조를 판단해야 한다.

핵심은 단순 평균 응답시간이 아니라, "고경합 상황에서 어느 방식이 더 안정적으로 같은 정합성을 유지하는가"다.
