package eureca.capstone.project.orchestrator.common.exception.custom;

public class UserDataNotFoundException extends RuntimeException {
    public UserDataNotFoundException() {
        super("사용자의 데이터 정보를 찾을 수 없습니다.");
    }
}
