package eureca.capstone.project.orchestrator.common.exception.custom;

public class QuizNotFoundException extends RuntimeException {
    public QuizNotFoundException() {
        super("퀴즈를 찾을 수 없습니다.");
    }
}
