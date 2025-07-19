package eureca.capstone.project.orchestrator.pay.dto.request;

import lombok.Data;

@Data
public class PaymentApprovalRequestDto {
    private String paymentKey;
    private String orderId;
    private Long amount; // 최종 결제 금액
}
