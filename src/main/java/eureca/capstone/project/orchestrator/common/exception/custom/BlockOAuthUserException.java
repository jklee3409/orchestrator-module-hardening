package eureca.capstone.project.orchestrator.common.exception.custom;

public class BlockOAuthUserException extends RuntimeException {
    public BlockOAuthUserException() {
        super("해당 계정은 차단된 OAuth User 입니다.");
    }
}
