package eureca.capstone.project.orchestrator.common.exception.custom;

public class TransactionFeedNotFoundException extends RuntimeException {
    public TransactionFeedNotFoundException() {
        super("해당 판매글을 찾을 수 없습니다.");
    }
}
