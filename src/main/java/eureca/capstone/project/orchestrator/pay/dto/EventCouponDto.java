package eureca.capstone.project.orchestrator.pay.dto;

import eureca.capstone.project.orchestrator.pay.entity.EventCoupon;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventCouponDto {
    private Long eventCouponId;
    private String couponNumber;
    private String couponName;
    private Long discountRate;
    private PayTypeDto payType;

    public static EventCouponDto fromEntity(EventCoupon eventCoupon) {
        return EventCouponDto.builder()
                .eventCouponId(eventCoupon.getEventCouponId())
                .couponNumber(eventCoupon.getCouponNumber())
                .couponName(eventCoupon.getCouponName())
                .discountRate(eventCoupon.getDiscountRate())
                .payType(PayTypeDto.fromEntity(eventCoupon.getPayType()))
                .build();
    }
}
