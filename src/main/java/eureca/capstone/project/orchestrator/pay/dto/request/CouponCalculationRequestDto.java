package eureca.capstone.project.orchestrator.pay.dto.request;

import lombok.Data;

@Data
public class CouponCalculationRequestDto {
    private Long userEventCouponId;
    private Long originalAmount;
}
