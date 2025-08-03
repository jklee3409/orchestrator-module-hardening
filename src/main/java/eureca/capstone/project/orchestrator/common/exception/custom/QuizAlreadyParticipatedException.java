package eureca.capstone.project.orchestrator.common.exception.custom;

public class QuizAlreadyParticipatedException extends RuntimeException {
    public QuizAlreadyParticipatedException() {
        super("이미 참여 완료되었습니다.");
    }
}
