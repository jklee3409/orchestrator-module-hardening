# Exception 규칙

## 필수 규칙

- 프로젝트 Service Layer에서 발생 가능한 모든 예외는 Custom Exception으로 처리해야 한다.
- ErrorCode는 /common/exception/code/ErrorCode.java에 작성한다.
- ErrorCode는 반드시 해당된 도메인 번호 구역을 따라야 한다.
- Custom Exception은 /common/exception/custom에 정의한다.
- 생성한 모든 Custom Exception은 /common/exception/GlobalExceptionHandler.java에서 처리한다.
- 프로젝트의 기존 Custom Exception을 참고하여 항상 같은 구조를 유지한다.

## 금지 규칙

- 기존에 존재하는 Custom Exception과 중복된 Exception을 새로 생성하지 않는다.
- 기존에 존재하는 ErrorCode와 중복된 내용의 ErrorCode를 새로 생성하지 않는다.
- 기존에 존재하는 Custom Exception을 임의로 삭제하지 않는다.
