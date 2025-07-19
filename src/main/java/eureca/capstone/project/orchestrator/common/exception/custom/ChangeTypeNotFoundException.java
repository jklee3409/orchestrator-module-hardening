package eureca.capstone.project.orchestrator.common.exception.custom;

public class ChangeTypeNotFoundException extends RuntimeException{
    public ChangeTypeNotFoundException() {super("페이 변동 유형을 찾을 수 없습니다.");}
}
