package eureca.capstone.project.orchestrator.pay.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponseDto {
    private String orderId;
    private String paymentMethod;
    private LocalDateTime completedAt;
}
