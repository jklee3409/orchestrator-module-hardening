package eureca.capstone.project.orchestrator.common.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    UNKNOWN_ERROR(10000, "UNKNOWN_ERROR", "알수없는 에러"),
    TOKEN_EXPIRED(10001, "TOKEN_EXPIRED", "Access Token 만료"),
    INVALID_SIGNATURE(10002, "INVALID_SIGNATURE", "JWT 서명 오류"),
    MALFORMED_TOKEN(10003, "MALFORMED_TOKEN", "JWT 구조 오류"),
    MISSING_TOKEN(10004, "MISSING_TOKEN", "JWT 누락"),
    REFRESH_TOKEN_MISMATCH(10005, "REFRESH_TOKEN_MISMATCH", "Redis 에 저장된 Refresh Token, 요청값의 Refresh Token 값 불일치"),
    EMAIL_TOKEN_MISMATCH(10006, "REFRESH_TOKEN_MISMATCH", "Redis 에 저장된 Email Token, 요청값의 Email Token 값 불일치"),
    BLACK_LIST_FOUND(10007, "BLACK_LIST_FOUND", "블랙리스트애 등록된 인원 입니다."),
    BLOCK_OAUTH_USER(10008, "BLOCK_OAUTH_USER", "해당 계정은 차단된 OAuth User 입니다.");

    private final int code;
    private final String name;
    private final String message;
}
