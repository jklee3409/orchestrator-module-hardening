package eureca.capstone.project.orchestrator.common.exception.custom;

public class SalesTypeNotFoundException extends RuntimeException{
    public SalesTypeNotFoundException() {super("판매 유형을 찾을 수 없습니다.");}
}
