package eureca.capstone.project.orchestrator.common.exception.custom;

public class AuctionCreationNotAllowedException extends RuntimeException {
    public AuctionCreationNotAllowedException() {
        super("입찰 판매 가능 시간이 아닙니다.");
    }
}
