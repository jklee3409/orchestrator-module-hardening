package eureca.capstone.project.orchestrator.common.exception.custom;

public class EmailAlreadyExistsException extends RuntimeException{
    public EmailAlreadyExistsException() {
        super("중복된 email 입니다.");
    }
}
