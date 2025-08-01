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
    BLOCK_USER(10008, "BLOCK_USER", "해당 계정은 차단된 User 입니다."),
    MISMATCH_USERID(10009,"MISMATCH_USERID","요청 ID 값과, TOKEN PARSING ID 값이 다릅니다."),

    // user 관련 에러코드 (20000 ~ 29999)

    // USER 관련 에러 코드 (20000 ~ 20050)
    USER_NOT_FOUND(20000, "USER_NOT_FOUND", "해당 사용자를 찾을 수 없습니다."),
    USER_EMAIL_ALREADY_EXISTS(20001, "USER_EMAIL_ALREADY_EXISTS", "중복된 email 입니다."),
    USER_CREATE_FAIL(20002, "USER_CREATE_FAIL", "사용자 등록 중 오류가 발생했습니다."),
    INVALID_PARAMETER(20003, "INVALID_PARAMETER", "유효하지 않은 파라미터입니다."),
    EMAIL_ALREADY_EXISTS(20004, "EMAIL_ALREADY_EXISTS", "이미 존재하는 이메일입니다."),
    PASSWORD_MISMATCH(20005, "PASSWORD_MISMATCH", "비밀번호가 일치하지 않습니다."),
    NEW_PASSWORD_SAME_AS_OLD(20006, "NEW_PASSWORD_SAME_AS_OLD", "새 비밀번호가 기존 비밀번호와 동일합니다."),
    PASSWORD_RESET_LINK_EXPIRED(20007, "PASSWORD_RESET_LINK_EXPIRED", "유효하지 않거나 만료된 비밀번호 재설정 링크입니다."),

    // USER_DATA 관련 에러 코드 (20051 ~ 20100)
    USER_DATA_CREATE_FAIL(20051, "USER_DATA_CREATE_FAIL", "사용자 데이터 등록 중 오류가 발생했습니다."),
    USER_TOTAL_DATA_LACK(20052, "USER_TOTAL_DATA_LACK", "사용자 보유 데이터가 부족합니다."),
    SELLABLE_DATA_CREATE_FAIL(20053, "SELLABLE_DATA_CREATE_FAIL", "보유 데이터에서 판매 가능한 데이터로 전환 중 오류가 발생했습니다"),
    USER_SELLABLE_DATA_LACK(20054, "USER_SELLABLE_DATA_LACK", "사용자 판매 가능 데이터가 부족합니다."),
    SELLABLE_DATA_DEDUCT_FAIL(20055, "SELLABLE_DATA_DEDUCT_FAIL", "판매 가능 데이터 차감 도중 오류가 발생했습니다."),
    SELLABLE_DATA_ADD_FAIL(20056, "SELLABLE_DATA_COMPENSATE_FAIL", "판매 가능데이터 증가 도중 오류가 발생했습니다"),
    BUYER_DATA_CHARGE_FAIL(20057, "BUYER_DATA_CHARGE_FAIL", "구매 데이터 충전 도중 오류가 발생했습니다."),
    BUYER_DATA_COMPENSATE_FAIL(20057, "BUYER_DATA_COMPENSATE_FAIL", "구매 데이터 충전 보상 도중 오류가 발생했습니다."),
    USER_DATA_NOT_FOUND(20058, "USER_DATA_NOT_FOUND", "사용자 데이터 정보를 찾지 못했습니다."),

    // PLAN 관련 에러 코드 (20101 ~ 20150)
    RANDOM_PLAN_RETURN_FAIL(20101, "RANDOM_PLAN_RETURN_FAIL", "랜덤 요금제 조회 중 오류가 발생했습니다."),
    EMPTY_PLAN(20102, "EMPTY_PLAN", "요금제가 존재하지 않습니다."),
    PLAN_NOT_FOUND(20103, "PLAN_NOT_FOUND", "요금제를 찾을 수 없습니다."),

    // transaction_feed 관련 에러코드 (30000 ~ 39999)
    TRANSACTION_FEED_CREATE_FAIL(30000, "TRANSACTION_FEED_CREATE_FAIL", "판매글 작성 도중 오류가 발생했습니다."),
    TRANSACTION_FEED_UPDATE_FAIL(30001, "TRANSACTION_FEED_UPDATE_FAIL", "판매글 수정 도중 오류가 발생했습니다."),
    FEED_MODIFY_PERMISSION_DENIED(30002, "FEED_MODIFY_PERMISSION_DENIED", "판매자가 아니므로 수정 또는 삭제가 불가합니다."),
    TRANSACTION_FEED_NOT_FOUND(30003, "TRANSACTION_FEED_NOT_FOUND", "거래글을 찾지 못하였습니다."),
    AUCTION_FEED_CREATE_FAIL(30004, "AUCTION_FEED_CREATE_FAIL", "입찰 판매 등록 가능 시간이 아닙니다"),
    SALES_TYPE_NOT_FOUND(30005, "SALES_TYPE_NOT_FOUND", "판매 유형을 찾지 못하였습니다."),
    AUCTION_FEED_MODIFY_NOT_ALLOWED(30006, "AUCTION_FEED_MODIFY_NOT_ALLOWED", "입찰 판매글은 수정 또는 삭제가 불가합니다."),
    ALREADY_EXISTS_LIKED_LIST(30007, "ALREADY_EXISTS_LIKED_LIST", "이미 찜한 판매글입니다."),
    WISH_FEED_NOT_FOUND(30008, "WISH_FEED_NOT_FOUND", "찜 목록에 해당 판매글이 없습니다."),
    SELLER_CANNOT_BID(30009, "SELLER_CANNOT_BID", "판매자는 자신의 판매글에 입찰할 수 없습니다."),
    AUCTION_NOT_ON_SALE(30010, "AUCTION_NOT_ON_SALE", "입찰 판매글이 판매 중이 아닙니다."),
    AUCTION_EXPIRED(30011, "AUCTION_EXPIRED", "입찰 판매글이 만료되었습니다."),
    BID_AMOUNT_TOO_LOW(30012, "BID_AMOUNT_TOO_LOW", "입찰 금액이 판매가보다 낮습니다."),
    CANNOT_BID_ON_OWN_HIGHEST(30013, "CANNOT_BID_ON_OWN_HIGHEST", "본인이 최고 입찰자일 때는 입찰할 수 없습니다."),
    FEED_NOT_AUCTION(30014, "FEED_NOT_AUCTION", "해당 판매글은 입찰 판매글이 아닙니다."),
    LUA_SCRIPT_ERROR(30015, "LUA_SCRIPT_ERROR", "루아 스크립트 실행 중 오류가 발생했습니다."),
    BID_PROCESSING_FAILED(30016, "BID_PROCESSING_FAILED", "입찰 처리 중 오류가 발생했습니다."),
    FEED_NOT_ON_SALE(30017, "FEED_NOT_ON_SALE", "판매글이 판매 중이 아닙니다."),
    CANNOT_BUY_OWN_FEED(30018, "CANNOT_BUY_OWN_FEED", "자신의 판매글을 구매할 수 없습니다."),
    CANNOT_BUY_AUCTION_FEED(30019, "CANNOT_BUY_AUCTION_FEED", "경매 판매글은 즉시 구매할 수 없습니다."),
    DATA_COUPON_NOT_FOUND(30020, "DATA_COUPON_NOT_FOUND", "데이터 쿠폰을 찾을 수 없습니다."),
    DATA_COUPON_ACCESS_DENIED(30021, "DATA_COUPON_ACCESS_DENIED", "데이터 쿠폰 소유권이 없습니다."),
    DATA_COUPON_ALREADY_USED(30022, "DATA_COUPON_ALREADY_USED", "이미 사용된 데이터 쿠폰입니다."),
    DATA_COUPON_EXPIRED(30023, "DATA_COUPON_EXPIRED", "만료된 데이터 쿠폰입니다."),
    TRANSACTION_HISTORY_NOT_FOUND(30024, "TRANSACTION_HISTORY_NOT_FOUND", "거래 내역을 찾을 수 없습니다."),
    BID_AMOUNT_100_DIVISIBLE(30025, "BID_AMOUNT_100_DIVISIBLE", "입찰 금액은 100원 단위로 입력해야 합니다."),

    // pay 관련 에러코드 (40000 ~ 49999)
    PAY_HISTORY_NOT_FOUND(40000, "PAY_HISTORY_NOT_FOUND", "페이 변동 내역을 찾을 수 없습니다."),
    CHARGE_HISTORY_NOT_FOUND(40001, "CHARGE_HISTORY_NOT_FOUND", "충전 내역을 찾을 수 없습니다."),
    EXCHANGE_HISTORY_NOT_FOUND(40002, "EXCHANGE_HISTORY_NOT_FOUND", "환전 내역을 찾을 수 없습니다."),

    // USER_EVENT_COUPON 관련 에러 코드 (40020 ~ 40050)
    USER_EVENT_COUPON_NOT_MATCHED(40020, "USER_EVENT_COUPON_NOT_MATCHED", "이벤트 쿠폰 소유자와 사용자가 다릅니다."),
    USER_EVENT_COUPON_EXPIRED(40021, "USER_EVENT_COUPON_EXPIRED", "사용되었거나 만료된 이벤트 쿠폰입니다."),
    USER_EVENT_COUPON_NOT_FOUND(40022, "USER_EVENT_NOT_FOUND", "사용자 이벤트 쿠폰을 찾을 수 없습니다."),
    EVENT_COUPON_NOT_FOUND(40023, "EVENT_NOT_FOUND", "이벤트 쿠폰을 찾을 수 없습니다."),
    USER_EVENT_COUPON_ALREADY_EXISTS(40024, "USER_EVENT_COUPON_ALREADY_EXISTS", "이미 발급된 이벤트 쿠폰입니다."),

    // PAY 관련 에러 코드 (40051 ~ 40100)
    PAY_TYPE_NOT_FOUND(40051, "PAY_TYPE_NOT_FOUND", "결제 수단을 찾지 못했습니다."),
    FINAL_AMOUNT_NOT_MATCHED(40052, "FINAL_AMOUNT_NOT_MATCHED", "최종 결제 금액이 일치하지 않습니다."),
    ORDER_NOT_FOUND(40053, "ORDER_NOT_FOUND", "주문 정보를 찾지 못했습니다."),
    ORDER_ALREADY_PROCESSED(40054, "ORDER_ALREADY_PROCESSED", "이미 처리된 주문입니다."),
    CHANGE_TYPE_NOT_FOUND(40055, "CHANGE_TYPE_NOT_FOUND", "페이 변동 유형을 찾지 못했습니다."),
    PAYMENT_CANCELLED_BY_PAY_METHOD(40056, "PAYMENT_CANCELLED_BY_PAY_METHOD", "결제 수단 불일치로 결제가 취소되었습니다."),
    PAYMENT_CANCELLED_FAIL(40056, "PAYMENT_CANCELLED_FAIL", "결제 승인 취소에 실패하였습니다."),
    USER_PAY_LACK(40057, "USER_PAY_LACK", "사용자 페이가 부족합니다."),
    USER_PAY_NOT_FOUND(40058, "USER_PAY_NOT_FOUND", "사용자 페이 정보를 찾지 못했습니다."),
    BANK_NOT_FOUND(40059, "BANK_NOT_FOUND", "은행 정보를 찾지 못했습니다."),

    // alarm 관련 에러코드 (50000 ~ 59999)
    ALARM_TYPE_NOT_FOUND(50000, "ALARM_TYPE_NOT_FOUND", "알람 유형을 찾지 못했습니다."),

    // common 관련 에러코드 (60000 ~ 69999)
    INVALID_TELECOM_COMPANY(60001, "INVALID_TELECOM_COMPANY", "통신사가 일치하지 않습니다."),
    STATUS_NOT_FOUND(60002, "STATUS_NOT_FOUND", "상태를 찾지 못하였습니다."),
    TELECOM_COMPANY_NOT_FOUND(60003, "TELECOM_COMPANY_NOT_FOUND", "통신사를 찾지 못하였습니다."),
    METHOD_ARGUMENT_NOT_VALID(60004, "METHOD_ARGUMENT_NOT_VALID", "입력 파라미터가 유효하지 않습니다. ex) 이메일, 제목, 내용...");

    private final int code;
    private final String name;
    private final String message;
}
