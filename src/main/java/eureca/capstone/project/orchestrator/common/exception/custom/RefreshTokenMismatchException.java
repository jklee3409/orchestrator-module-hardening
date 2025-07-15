package eureca.capstone.project.orchestrator.common.exception.custom;

public class RefreshTokenMismatchException extends RuntimeException {
    public RefreshTokenMismatchException() {
        super("Redis 에 저장된 Refresh Token, 요청값의 Refresh Token 값 불일치");
    }
}
