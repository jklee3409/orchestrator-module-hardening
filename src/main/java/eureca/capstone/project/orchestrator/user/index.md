# user 도메인 인덱스

## 역할

`user`는 user profile, user data balance, plan data, password reset, sellable data, buyer data operation을 담당한다.

## 구조

- `controller`: user, user data, password reset endpoint
- `service`: user profile, user data, plan, password reset business logic
- `repository`: user, user data, plan persistence query
- `entity`: `User`, `UserData`, `Plan`
- `dto`: user, user data, plan, password reset request/response DTO

## 핵심 진입점

- `UserServiceImpl`
- `UserDataServiceImpl`
- `PlanServiceImpl`
- `PasswordResetServiceImpl`
- `UserRepositoryImpl`
- `UserDataRepositoryImpl`

## 에이전트 규칙

- 이 도메인을 수정하기 전에 이 파일과 `progress.md`를 읽어야 한다.
- user data mutation은 transaction feed purchase/sale flow와 정합성을 유지해야 한다.
- 이 도메인에서 authentication 또는 Pay logic을 중복 구현하면 안 된다.
- 이 도메인 하위 변경 후 `progress.md`를 갱신해야 한다.
