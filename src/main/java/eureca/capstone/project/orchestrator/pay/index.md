# pay 도메인 인덱스

## 역할

`pay`는 Pay balance, charge, use, refund, exchange, coupon, payment approval, webhook, Pay history flow를 담당한다.

## 구조

- `controller`: Pay, payment, history, coupon, exchange, webhook endpoint
- `service`: Pay mutation, payment transaction, Toss approval, webhook, history, exchange, coupon logic
- `repository`: Pay, charge, exchange, coupon, bank, type, history persistence query
- `entity`: Pay balance, Pay history, charge/exchange history, coupon, bank, type entity
- `dto`: request, response, shared Pay DTO

## 핵심 진입점

- `UserPayServiceImpl`
- `PaymentServiceImpl`
- `PaymentTransactionServiceImpl`
- `WebhookServiceImpl`
- `PayHistoryServiceImpl`
- `ExchangeServiceImpl`

## 에이전트 규칙

- 이 도메인을 수정하기 전에 이 파일과 `progress.md`를 읽어야 한다.
- payment key, webhook, Pay state 변경 시 `.codex/rules/data-integrity.md`와 `.codex/rules/security.md`를 읽어야 한다.
- Pay mutation은 transactional하게 유지하고 committed domain state 기준으로 처리해야 한다.
- 이 도메인 하위 변경 후 `progress.md`를 갱신해야 한다.
