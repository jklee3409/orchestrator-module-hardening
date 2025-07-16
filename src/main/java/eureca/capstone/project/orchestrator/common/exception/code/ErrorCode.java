package eureca.capstone.project.orchestrator.common.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // auth 관련 에러코드 (10000 ~ 19999)
    UNKNOWN_ERROR(10000, "UNKNOWN_ERROR", "알수없는 에러"),
    TOKEN_EXPIRED(10001, "TOKEN_EXPIRED", "Access Token 만료"),
    INVALID_SIGNATURE(10002, "INVALID_SIGNATURE", "JWT 서명 오류"),
    MALFORMED_TOKEN(10003, "MALFORMED_TOKEN", "JWT 구조 오류"),
    MISSING_TOKEN(10004, "MISSING_TOKEN", "JWT 누락"),
    REFRESH_TOKEN_MISMATCH(10005, "REFRESH_TOKEN_MISMATCH", "Redis 에 저장된 Refresh Token, 요청값의 Refresh Token 값 불일치"),
    EMAIL_TOKEN_MISMATCH(10006, "REFRESH_TOKEN_MISMATCH", "Redis 에 저장된 Email Token, 요청값의 Email Token 값 불일치"),
    BLACK_LIST_USER_FOUND(10007, "BLACK_LIST_USER_FOUND", "해당 사용자는 BlackList 포함된 사용자 입니다."),

    // user 관련 에러코드 (20000 ~ 29999)

    // USER 관련 에러 코드 (20000 ~ 20050)
    USER_NOT_FOUND(20000, "USER_NOT_FOUND", "해당 사용자를 찾을 수 없습니다."),
    USER_EMAIL_ALREADY_EXISTS(20001, "USER_EMAIL_ALREADY_EXISTS", "중복된 email 입니다."),
    USER_CREATE_FAIL(20002, "USER_CREATE_FAIL", "사용자 등록 중 오류가 발생했습니다."),
    INVALID_PARAMETER(20003, "INVALID_PARAMETER", "유효하지 않은 파라미터입니다."),

    // USER_DATA 관련 에러 코드 (20051 ~ 20100)
    USER_DATA_CREATE_FAIL(20051, "USER_DATA_CREATE_FAIL", "사용자 데이터 등록 중 오류가 발생했습니다."),
    USER_TOTAL_DATA_LACK(20052, "USER_TOTAL_DATA_LACK", "사용자 보유 데이터가 부족합니다."),
    SELLABLE_DATA_CREATE_FAIL(20053, "SELLABLE_DATA_CREATE_FAIL", "보유 데이터에서 판매 가능한 데이터로 전환 중 오류가 발생했습니다"),
    USER_SELLABLE_DATA_LACK(20054, "USER_SELLABLE_DATA_LACK", "사용자 판매 가능 데이터가 부족합니다."),
    SELLABLE_DATA_DEDUCT_FAIL(20055, "SELLABLE_DATA_DEDUCT_FAIL", "판매 가능 데이터 차감 도중 오류가 발생했습니다."),
    SELLABLE_DATA_COMPENSATE_FAIL(20056, "SELLABLE_DATA_COMPENSATE_FAIL", "판매 가능데이터 차감 보상 도중 오류가 발생했습니다"),
    BUYER_DATA_CHARGE_FAIL(20057, "BUYER_DATA_CHARGE_FAIL", "구매 데이터 충전 도중 오류가 발생했습니다."),
    BUYER_DATA_COMPENSATE_FAIL(20057, "BUYER_DATA_COMPENSATE_FAIL", "구매 데이터 충전 보상 도중 오류가 발생했습니다."),

    // PLAN 관련 에러 코드 (20101 ~ 20150)
    RANDOM_PLAN_RETURN_FAIL(20101, "RANDOM_PLAN_RETURN_FAIL", "랜덤 요금제 조회 중 오류가 발생했습니다.");


    // transaction_feed 관련 에러코드 (30000 ~ 39999)

    // pay 관련 에러코드 (40000 ~ 49999)

    // alarm 관련 에러코드 (50000 ~ 59999)

    private final int code;
    private final String name;
    private final String message;
}
