# event 도메인 인덱스

## 역할

`event`는 quiz event, quiz participation, quiz status 변경, Pay service로의 reward handoff를 담당한다.

## 구조

- `controller`: quiz endpoint
- `service`: quiz participation 및 status business logic
- `repository`: quiz 및 participation persistence query
- `entity`: `Quiz`, `QuizParticipation`
- `dto`: quiz request 및 response DTO

## 핵심 진입점

- `QuizController`
- `QuizServiceImpl`
- `QuizRepository`
- `QuizParticipationRepository`

## 에이전트 규칙

- 이 도메인을 수정하기 전에 이 파일과 `progress.md`를 읽어야 한다.
- reward payment 동작은 Pay service에 위임해야 한다.
- 이 도메인에서 Pay balance mutation logic을 중복 구현하면 안 된다.
- 이 도메인 하위 변경 후 `progress.md`를 갱신해야 한다.
