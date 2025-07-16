package eureca.capstone.project.orchestrator.common.exception.custom;

public class BlackListUserException extends RuntimeException {
    public BlackListUserException() {
        super("해당 사용자는 BlackList 포함된 사용자 입니다.");
    }
}
