package eureca.capstone.project.orchestrator.common.exception.custom;

public class FeedModifyPermissionException extends RuntimeException {
    public FeedModifyPermissionException() {
        super("게시글을 수정할 수 있는 권한이 없습니다.");
    }
}
