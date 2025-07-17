package eureca.capstone.project.orchestrator.common.exception.custom;

public class AuctionTypeModifyNotAllowedException extends RuntimeException {
    public AuctionTypeModifyNotAllowedException() {
        super("입찰 판매는 수정 또는 삭제가 불가능합니다.");
    }
}
