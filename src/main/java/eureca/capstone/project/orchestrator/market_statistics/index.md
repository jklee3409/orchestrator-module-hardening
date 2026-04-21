# market_statistics 도메인 인덱스

## 역할

`market_statistics`는 market price statistic과 carrier/hourly aggregation view를 제공한다.

## 구조

- `controller`: statistics endpoint
- `service`: aggregation 및 read model logic
- `repository`: market statistics persistence access
- `entity`: `MarketStatistic`
- `dto`: hourly 및 carrier price statistic DTO

## 핵심 진입점

- `MarketStatisticController`
- `MarketStatisticServiceImpl`
- `MarketStatisticsRepository`

## 에이전트 규칙

- 이 도메인을 수정하기 전에 이 파일과 `progress.md`를 읽어야 한다.
- statistics read는 persisted transaction data 또는 explicit statistic record에서 파생해야 한다.
- 이 도메인에서 Pay, user data, transaction feed state를 변경하면 안 된다.
- 이 도메인 하위 변경 후 `progress.md`를 갱신해야 한다.
