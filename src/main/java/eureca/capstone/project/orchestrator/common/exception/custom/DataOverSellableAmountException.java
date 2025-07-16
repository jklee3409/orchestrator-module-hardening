package eureca.capstone.project.orchestrator.common.exception.custom;

public class DataOverSellableAmountException extends RuntimeException {
    public DataOverSellableAmountException() {
        super("판매 가능 데이터 양을 초과하였습니다.");
    }
}
