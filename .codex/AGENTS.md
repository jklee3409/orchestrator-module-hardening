# AGENTS.md

## 0. 목적

이 파일은 Datcha Orchestrator에서 작업하는 AI 코딩 에이전트의 필수 진입 문서이다.

에이전트는 이 파일을 항상 컨텍스트에 유지해야 한다. 에이전트는 작업 범위와 관련된 경우에만 추가 규칙 파일을 읽어야 한다. 이 파일은 사람용 프로젝트 설명서가 아니라 에이전트 행동 제어 문서이다.

## 1. 절대 규칙

### 필수

- 기존 Spring Boot 계층 구조를 유지해야 한다: controller -> service -> repository.
- 거래 정합성의 source of truth는 MySQL로 고정해야 한다.
- Redis는 조정, 캐시, 세션, Pub/Sub 인프라로만 사용해야 하며 최종 거래 진실로 사용하면 안 된다.
- 외부 부작용은 commit 이후 event handler에서 실행해야 한다.
- 변경은 사용자 요청 범위 안에서 최소화해야 한다.
- 신규 추상화보다 기존 manager, service, repository, DTO 패턴, exception을 먼저 재사용해야 한다.
- 동작이 바뀐 경로는 focused verification을 실행해야 한다.

### 금지

- pipeline 순서를 변경하면 안 된다.
- Redis Lua 성공을 최종 비즈니스 성공으로 처리하면 안 된다.
- DB commit 전에 Elasticsearch 갱신 또는 Kafka 알림 발행을 하면 안 된다.
- 명시적 사용자 승인 없이 domain table을 통합하거나 schema 의미를 바꾸면 안 된다.
- production profile에 local test bypass 값, secret, benchmark 기본값을 추가하면 안 된다.
- 요청과 무관한 대규모 refactor를 하면 안 된다.
- 작업을 통과시키기 위해 test, assertion, authentication, payment validation, bid validation을 약화하면 안 된다.

## 2. 핵심 Pipeline 고정

### 거래 요청 Pipeline

1. API 요청 수신
2. 사용자 인증
3. Request DTO 검증
4. domain 사전 조건 검증
5. manager를 통한 status/type 조회
6. 동시성 제어 적용
7. Pay 상태 검증 및 갱신
8. MySQL 상태 저장
9. domain event 발행
10. transaction commit
11. AFTER_COMMIT handler 실행
12. Elasticsearch projection 갱신
13. Kafka alarm 발행
14. Redis Pub/Sub 및 SSE notification 전달
15. API 응답 반환

### Bid Pipeline

1. bid 요청 수신
2. 인증 사용자 식별
3. feed, sale type, sale status, amount, self-bid 조건 검증
4. Redis Lua 최고가 선반영
5. DB lock 기반 committed state 확정
6. DB 상태 기준 이전 최고 입찰자 환불
7. 신규 입찰자 Pay 차감
8. bid history 저장
9. `BidSucceededEvent` 발행
10. transaction commit
11. `BidSucceededEventHandler`를 commit 이후 실행

## 3. 규칙 로딩 표

에이전트는 수정 전에 작업 범위를 식별해야 하며, 일치하는 모든 규칙 파일을 읽어야 한다.

| 범위 | 대상 변경 | 필수 파일 |
|---|---|---|
| Architecture | Controller, Service, Repository, Event, package 구조 | `.codex/rules/architecture.md` |
| Bid | bid, auction, Redis Lua, DB lock, bid Pay 흐름 | `.codex/rules/bid-pipeline.md` |
| Data | Entity, table, Redis key, Elasticsearch document, Kafka topic | `.codex/rules/data-integrity.md` |
| Security | JWT, OAuth, filter, authorization, JMeter bypass, profile security | `.codex/rules/security.md` |
| Testing | test 생성, test 수정, 검증 전략 | `.codex/rules/testing.md` |
| Benchmark | `jmeter/`, seed SQL, benchmark CSV, benchmark properties | `.codex/rules/benchmark.md` |
| Exception | Service Layer 예외 처리, ErrorCode, GlobalExceptionHandler | `.codex/rules/exception.md` |

## 4. 도메인 문서와 진행 기록

`src/main/java/eureca/capstone/project/orchestrator/` 하위 모든 domain directory는 다음 파일을 반드시 가져야 한다.

- `index.md`: domain 역할, 구조, 핵심 entry point, agent 규칙
- `progress.md`: 해당 domain의 간결한 변경 이력

### 필수 Agent Workflow

1. 수정 대상 domain directory를 모두 식별한다.
2. 수정 전에 각 대상 domain의 `index.md`와 `progress.md`를 읽는다.
3. 둘 중 하나라도 없으면 해당 domain code 변경 전에 먼저 생성한다.
4. 작업에 필요한 최소 파일만 수정한다.
5. code 또는 도메인 문서를 변경한 뒤 각 대상 domain의 `progress.md`를 갱신한다.
6. 최종 응답에 갱신한 domain progress 파일을 명시한다.

### 진행 기록 형식

domain `progress.md`를 갱신할 때는 다음 형식을 사용해야 한다.

```md
### YYYY-MM-DD - <짧은 제목>
- 범위:
- 파일:
- 변경:
- 검증:
- 위험:
```

### 금지

- domain `index.md`와 `progress.md`를 읽지 않고 domain code를 수정하면 안 된다.
- 관련 없는 global 변경을 domain progress 파일에 기록하면 안 된다.
- progress 파일을 긴 서술형 문서로 만들면 안 된다. 최신순으로 간결하게 유지해야 한다.

## 5. 수정 전략

1. 관련 controller, service, repository, DTO, entity, event, config, test 파일의 기존 흐름을 분석한다.
2. public contract와 transaction boundary를 유지하면서 영향 범위를 최소화한다.
3. 기존 구조와 manager/service/repository 패턴에 통합한다.
4. 기존 코드로 요청을 만족할 수 없을 때만 신규 구현을 추가한다.
5. 변경된 동작은 focused test로 검증하거나 검증하지 못한 이유를 명시한다.

## 6. 출력 요구사항

최종 응답은 반드시 다음 항목을 포함해야 한다.

1. 변경 요약
2. 변경 파일
3. 검증 결과
4. 남은 위험 또는 확인 필요 사항
5. 다음 실행 가능한 단계

동작 변경이 있는 경우 DB schema, Redis key, Kafka topic, Elasticsearch document, external API, security profile, transaction boundary 변경 여부도 반드시 명시해야 한다.
