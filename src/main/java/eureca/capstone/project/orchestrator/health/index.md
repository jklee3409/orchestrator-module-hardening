# health 도메인 인덱스

## 역할

`health`는 deployment 및 operational check를 위한 lightweight service health endpoint를 제공한다.

## 구조

- `controller`: health check endpoint

## 핵심 진입점

- `HealthCheckController`

## 에이전트 규칙

- 이 도메인을 수정하기 전에 이 파일과 `progress.md`를 읽어야 한다.
- health check는 lightweight 및 side-effect free로 유지해야 한다.
- 명시적 요청 없이 database, Redis, Kafka, Elasticsearch, external API call을 추가하면 안 된다.
- 이 도메인 하위 변경 후 `progress.md`를 갱신해야 한다.
