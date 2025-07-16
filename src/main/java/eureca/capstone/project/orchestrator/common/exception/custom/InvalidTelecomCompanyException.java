package eureca.capstone.project.orchestrator.common.exception.custom;

public class InvalidTelecomCompanyException extends RuntimeException {
    public InvalidTelecomCompanyException() {
        super("사용자 통신사와 일치하지 않습니다.");
    }
}
