package eureca.capstone.project.orchestrator.common.exception.custom;

public class EmptyPlanException extends RuntimeException{
    public EmptyPlanException() {
        super("랜덤 요금제 조회 중 오류가 발생했습니다.");
    }
}
