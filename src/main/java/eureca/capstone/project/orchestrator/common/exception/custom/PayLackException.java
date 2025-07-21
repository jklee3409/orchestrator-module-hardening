package eureca.capstone.project.orchestrator.common.exception.custom;

public class PayLackException extends RuntimeException {
    public PayLackException() {
        super("사용자 페이가 부족합니다.");
    }
}
