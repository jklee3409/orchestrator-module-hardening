package eureca.capstone.project.orchestrator.common.exception.custom;

public class BlockUserException extends RuntimeException {
    public BlockUserException() {
        super("해당 계정은 차단된 User 입니다.");
    }
}
