package eureca.capstone.project.orchestrator.pay.dto;

import eureca.capstone.project.orchestrator.pay.entity.EventCoupon;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventCouponDto {
    private Long eventCouponId;
    private String couponName;
    private String couponDescription;
    private Long discountRate;
    private PayTypeDto payType;

    public static EventCouponDto fromEntity(EventCoupon eventCoupon) {
        return EventCouponDto.builder()
                .eventCouponId(eventCoupon.getEventCouponId())
                .couponDescription(eventCoupon.getCouponDescription())
                .couponName(eventCoupon.getCouponName())
                .discountRate(eventCoupon.getDiscountRate())
                .payType(
                        eventCoupon.getPayType() != null
                        ? PayTypeDto.fromEntity(eventCoupon.getPayType())
                        : null)
                .build();
    }
}
