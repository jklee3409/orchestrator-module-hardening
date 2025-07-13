package eureca.capstone.project.orchestrator.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    UNKNOWN_ERROR(10000, "UNKNOWN_ERROR", "알수없는 에러"),
    NOT_SUCCESS_STATUS_CODE_EXCEPTION(10001, "NOT_SUCCESS_STATUS_CODE_EXCEPTION", "API 결과가 정상적이지 않습니다.");

    private final int code;
    private final String name;
    private final String message;
}
