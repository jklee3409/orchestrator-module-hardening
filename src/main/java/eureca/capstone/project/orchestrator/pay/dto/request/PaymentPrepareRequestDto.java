package eureca.capstone.project.orchestrator.pay.dto.request;

import lombok.Data;

@Data
public class PaymentPrepareRequestDto {
    private Long userEventCouponId;
    private Long originalAmount;
    private Long finalAmount;
}
