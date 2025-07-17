package eureca.capstone.project.orchestrator.pay.dto.response;

import eureca.capstone.project.orchestrator.pay.dto.PayTypeDto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CouponCalculationResponseDto {
    private Long originalAmount;
    private Long discountAmount;
    private Long finalAmount;
    private PayTypeDto requiredPayType;
}
