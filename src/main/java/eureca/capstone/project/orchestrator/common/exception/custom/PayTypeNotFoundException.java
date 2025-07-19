package eureca.capstone.project.orchestrator.common.exception.custom;

public class PayTypeNotFoundException extends RuntimeException {
    public PayTypeNotFoundException() {
        super("해당 결제 수단을 찾을 수 없습니다.");
    }
}
