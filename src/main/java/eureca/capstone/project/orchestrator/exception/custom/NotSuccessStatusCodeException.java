package eureca.capstone.project.orchestrator.exception.custom;

public class NotSuccessStatusCodeException extends RuntimeException {
    public NotSuccessStatusCodeException() {
        super("API 결과가 정상적이지 않습니다.");
    }
}
