package eureca.capstone.project.orchestrator.pay.dto.response;

import eureca.capstone.project.orchestrator.pay.dto.PayTypeDto;
import eureca.capstone.project.orchestrator.pay.entity.ChargeHistory;
import eureca.capstone.project.orchestrator.pay.entity.PayType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentPrepareResponseDto {
    private String orderId;
    private Long originalAmount;
    private Long discountAmount;
    private Long finalAmount;
    private PayTypeDto requiredPayType;

    public PaymentPrepareResponseDto(ChargeHistory chargeHistory, PayType payType) {
        this.orderId = chargeHistory.getOrderId();
        this.originalAmount = chargeHistory.getAmount();
        this.discountAmount = chargeHistory.getDiscountAmount();
        this.finalAmount = chargeHistory.getChargePay();
        this.requiredPayType = (payType != null) ? PayTypeDto.fromEntity(payType) : null;
    }
}
