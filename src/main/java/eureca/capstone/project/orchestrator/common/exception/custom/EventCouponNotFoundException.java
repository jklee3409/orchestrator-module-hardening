package eureca.capstone.project.orchestrator.common.exception.custom;

public class EventCouponNotFoundException extends RuntimeException {
    public EventCouponNotFoundException() {
        super("이벤트 쿠폰을 찾을 수 없습니다.");
    }
}
