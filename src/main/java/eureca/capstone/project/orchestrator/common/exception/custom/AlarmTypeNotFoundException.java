package eureca.capstone.project.orchestrator.common.exception.custom;

public class AlarmTypeNotFoundException extends RuntimeException {
    public AlarmTypeNotFoundException() {
        super("해당 알림 유형을 찾을 수 없습니다.");
    }
}
