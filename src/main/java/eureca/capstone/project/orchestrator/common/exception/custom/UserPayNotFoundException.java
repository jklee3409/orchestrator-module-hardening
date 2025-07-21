package eureca.capstone.project.orchestrator.common.exception.custom;

public class UserPayNotFoundException extends RuntimeException {
  public UserPayNotFoundException() {
    super("사용자 페이 정보를 찾을 수 없습니다.");
  }
}
