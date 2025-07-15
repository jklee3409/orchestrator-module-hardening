package eureca.capstone.project.orchestrator.common.exception.custom;

import eureca.capstone.project.orchestrator.common.exception.code.ErrorCode;
import lombok.Getter;

@Getter
public class InternalServerException extends RuntimeException{

    private final ErrorCode errorCode;

    public InternalServerException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
