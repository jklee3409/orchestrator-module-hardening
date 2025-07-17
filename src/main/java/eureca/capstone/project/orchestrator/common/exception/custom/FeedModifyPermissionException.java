package eureca.capstone.project.orchestrator.common.exception.custom;

public class FeedModifyPermissionException extends RuntimeException {
    public FeedModifyPermissionException() {
        super("게시글을 수정하거나 삭제할 수 없습니다. 판매자가 아닙니다.");
    }
}
