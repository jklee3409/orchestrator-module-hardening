# alarm 도메인 인덱스

## 역할

`alarm`은 저장된 알림, Kafka 기반 알림 생성, Redis Pub/Sub 전달, SSE notification streaming을 담당한다.

## 구조

- `controller`: notification 조회 및 stream endpoint
- `service`: alarm business logic, Kafka producer/consumer flow, SSE emitter 관리, Redis subscriber
- `repository`: alarm 및 alarm type persistence query
- `entity`: `Alarm`, `AlarmType`
- `dto`: alarm creation, alarm type, notification, read request DTO
- `config`: Kafka topic, producer, consumer 설정

## 핵심 진입점

- `NotificationController`
- `AlarmServiceImpl`
- `NotificationService`
- `NotificationProducer`
- `SseEmitterService`
- `RedisNotificationSubscriber`

## 에이전트 규칙

- 이 도메인을 수정하기 전에 이 파일과 `progress.md`를 읽어야 한다.
- Kafka, Redis Pub/Sub, SSE delivery는 commit 이후 side effect로 유지해야 한다.
- alarm delivery를 DB transaction 성공의 필수 조건으로 만들면 안 된다.
- 이 도메인 하위 변경 후 `progress.md`를 갱신해야 한다.
