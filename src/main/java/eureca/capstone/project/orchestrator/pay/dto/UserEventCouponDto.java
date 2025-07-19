package eureca.capstone.project.orchestrator.pay.dto;

import eureca.capstone.project.orchestrator.common.dto.StatusDto;
import eureca.capstone.project.orchestrator.pay.entity.UserEventCoupon;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserEventCouponDto {
    private Long userEventCouponId;
    private LocalDateTime expiresAt;
    private StatusDto status;
    private EventCouponDto eventCoupon;

    public static UserEventCouponDto fromEntity(UserEventCoupon userEventCoupon) {
        return UserEventCouponDto.builder()
                .userEventCouponId(userEventCoupon.getUserEventCouponId())
                .expiresAt(userEventCoupon.getExpiresAt())
                .status(StatusDto.fromEntity(userEventCoupon.getStatus()))
                .eventCoupon(EventCouponDto.fromEntity(userEventCoupon.getEventCoupon()))
                .build();
    }
}
