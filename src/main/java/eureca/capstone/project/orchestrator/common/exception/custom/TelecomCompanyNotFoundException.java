package eureca.capstone.project.orchestrator.common.exception.custom;

public class TelecomCompanyNotFoundException extends RuntimeException {
    public TelecomCompanyNotFoundException() {
        super("해당 통신사를 찾을 수 없습니다.");
    }
}
