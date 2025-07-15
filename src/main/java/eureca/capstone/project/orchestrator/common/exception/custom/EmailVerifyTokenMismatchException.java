package eureca.capstone.project.orchestrator.common.exception.custom;

public class EmailVerifyTokenMismatchException extends RuntimeException {
    public EmailVerifyTokenMismatchException() {
        super("Redis 에 저장된 Email Token, 요청값의 Email Token 값 불일치");
    }
}
