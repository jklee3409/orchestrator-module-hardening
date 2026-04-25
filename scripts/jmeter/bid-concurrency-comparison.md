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

`JwtAuthenticationFilter`에는 non-prod 환경에서만 사용할 수 있는 JMeter 인증 우회 기능이 포함되어 있다.

서버 실행 전 아래 값을 별도로 설정해야 한다.

- `SECURITY_JMETER_BYPASS_ENABLED=true`
- `SECURITY_JMETER_BYPASS_TEST_KEY={테스트용 비밀값}`

로컬 서버 실행 기준:

- 일반 로컬 실행만 할 때는 설정하지 않아도 된다.
- JMeter 우회 인증으로 부하 테스트를 수행할 때만 설정하면 된다.

IntelliJ 설정 예시:

- `Run/Debug Configurations` > `OrchestratorApplication` > `Environment variables`
- `SECURITY_JMETER_BYPASS_ENABLED=true`
- `SECURITY_JMETER_BYPASS_TEST_KEY=local-jmeter-secret`

PowerShell 설정 예시:

```powershell
$env:SECURITY_JMETER_BYPASS_ENABLED='true'
$env:SECURITY_JMETER_BYPASS_TEST_KEY='local-jmeter-secret'
```

요청 헤더는 아래와 같이 설정한다.

- `X-JMeter-Test-Key: ${SECURITY_JMETER_BYPASS_TEST_KEY}`
- `Authorization: Bearer ${user_email}`
- `Content-Type: application/json`

주의:

- 운영 프로필에서는 해당 우회 기능이 비활성화된다.
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

### 현재 `bid-benchmark.jmx` 설계

현재 JMX는 한 번의 GUI 실행에서 아래 순서로 두 구현을 모두 실행하도록 설계한다.

1. `CONFIG_VALIDATE`
2. `REDIS_WARMUP`
3. `REDIS_MEASURE`
4. `DB_LOCK_WARMUP`
5. `DB_LOCK_MEASURE`

중요 포인트:

- Test Plan에서 thread group을 **직렬 실행**한다.
- warm-up feed와 measured feed를 분리해 JIT, thread start, 첫 Redis/DB access 비용이 측정 feed 상태를 오염시키지 않게 한다.
- 구현별 bidder CSV도 분리해 한 번의 실행 안에서도 사용자 풀을 섞지 않는다.
- `workloadType`은 `winner` 또는 `loser`만 사용한다.
- measured run은 `loops=1`, `think time=0`을 유지해 "한 유저당 한 번 입찰" hot path만 비교한다.

GUI에서 실행하기 전 `jmeter/setup-bid-benchmark.sql` 결과에서 아래 값을 복사해 JMX 변수에 넣어야 한다.

- `redis_warmup_winner_feed_id`
- `redis_warmup_loser_feed_id`
- `redis_winner_feed_id`
- `redis_loser_feed_id`
- `db_lock_warmup_winner_feed_id`
- `db_lock_warmup_loser_feed_id`
- `db_lock_winner_feed_id`
- `db_lock_loser_feed_id`

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

1. `jmeter/setup-bid-benchmark.sql`을 다시 실행한다.
2. SQL 결과에서 warm-up/measured feed id, bidder count, test key를 확인한다.
3. 서버를 `SECURITY_JMETER_BYPASS_ENABLED=true`와 대응하는 test key로 실행한다.
4. JMeter GUI에서 `bid-benchmark.jmx`를 열고 feed id 변수를 채운다.
5. `workloadType=winner`로 1회 실행해 고경합 winner path를 비교한다.
6. SQL을 다시 실행해 상태를 초기화한다.
7. `workloadType=loser`로 1회 실행해 fast-fail loser path를 비교한다.
8. 각 실행 뒤 DB 최고가, bid history, user_pay를 확인한다.

## 1차 권장 부하 구간

- 50 measured users / warm-up 20 users / loop 1
- 100 measured users / warm-up 25 users / loop 1
- 200 measured users / warm-up 50 users / loop 1

필요 시 아래 순서로 확장한다.

- 300 measured users / warm-up 50 users / loop 1
- 500 measured users / warm-up 50 users / loop 1

실무 팁:

- 처음부터 큰 부하로 가지 말고, 작은 구간에서 오류 패턴과 데이터 정합성을 먼저 확인한 뒤 확대하는 편이 안전하다.
- 응답시간만 보지 말고 실패 건의 성격도 함께 확인해야 한다.
- 500은 현재 seed SQL이 준비하는 bidder 수 상한이므로, 그 이상은 CSV와 SQL을 함께 늘리지 않으면 안 된다.
- 포트폴리오용 수치는 최소 3회 반복 실행 후 median/p95 기준으로 정리하고, 각 실행 전에는 seed SQL을 다시 적용해 feed/user_pay 상태를 초기화하는 편이 좋다.

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
