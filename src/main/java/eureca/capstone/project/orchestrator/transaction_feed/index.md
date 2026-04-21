# transaction_feed 도메인 인덱스

## 역할

`transaction_feed`는 normal data sale, auction, bid, feed search, recommendation, like, data purchase, coupon, transaction history를 담당한다.

## 구조

- `controller`: feed, bid, purchase, coupon, history, recommendation, like, Elasticsearch admin endpoint
- `service`: feed lifecycle, bid flow, purchase flow, coupon use, history, recommendation, like logic
- `repository`: feed, bid, like, coupon, search, history를 위한 JPA 및 QueryDSL persistence query
- `entity`: feed, bid, sales type, like, data coupon, transaction history entity
- `event`: bid success event 및 after-commit handler
- `document`: Elasticsearch projection
- `dto`: request, response, enum, shared transaction feed DTO

## 핵심 진입점

- `TransactionFeedServiceImpl`
- `BidServiceImpl`
- `BidServiceWithLock`
- `DataFeedPurchaseServiceImpl`
- `BidSucceededEventHandler`
- `TransactionFeedSearchRepositoryImpl`
- `BidsRepositoryImpl`

## 에이전트 규칙

- 이 도메인을 수정하기 전에 이 파일과 `progress.md`를 읽어야 한다.
- bid 또는 auction 변경 시 `.codex/rules/bid-pipeline.md`를 읽어야 한다.
- Redis prewrite, DB confirmation, Pay mutation, event publish, after-commit side effect를 분리해서 유지해야 한다.
- 이 도메인 하위 변경 후 `progress.md`를 갱신해야 한다.
