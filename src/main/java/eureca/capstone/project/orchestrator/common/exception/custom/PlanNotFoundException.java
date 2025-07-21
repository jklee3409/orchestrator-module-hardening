package eureca.capstone.project.orchestrator.common.exception.custom;

public class PlanNotFoundException extends RuntimeException {
    public PlanNotFoundException() {
        super("요금제를 찾을 수 없습니다.");
    }
}
