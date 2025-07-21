package eureca.capstone.project.orchestrator.common.exception.custom;

import eureca.capstone.project.orchestrator.common.exception.code.ErrorCode;
import lombok.Getter;

@Getter
public class BidException extends RuntimeException{

    private final ErrorCode errorCode;

    public BidException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
