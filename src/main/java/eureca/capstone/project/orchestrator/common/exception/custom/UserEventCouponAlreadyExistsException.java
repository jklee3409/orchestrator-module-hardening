package eureca.capstone.project.orchestrator.common.exception.custom;

public class UserEventCouponAlreadyExistsException extends RuntimeException {
    public UserEventCouponAlreadyExistsException() {
        super("이미 발급된 이벤트 쿠폰입니다.");
    }
}
