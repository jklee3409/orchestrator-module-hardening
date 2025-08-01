package eureca.capstone.project.orchestrator.common.exception.custom;

public class MismatchUserIdException extends RuntimeException {
    public MismatchUserIdException() {
        super("요청 ID 값과, TOKEN PARSING ID 값이 다릅니다.");
    }
}
