package eureca.capstone.project.orchestrator.pay.dto;

import eureca.capstone.project.orchestrator.pay.entity.PayHistory;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PayHistoryDto {
    private Long payHistoryId;
    private ChangeTypeDto changeType;
    private Long changedPay; // 변동 페이
    private Long finalPay; // 최종 페이
    private LocalDateTime createdAt; // 변동 일시

    public static PayHistoryDto fromEntity(PayHistory payHistory) {
        return PayHistoryDto.builder()
                .payHistoryId(payHistory.getPayHistoryId())
                .changeType(ChangeTypeDto.fromEntity(payHistory.getChangeType()))
                .changedPay(payHistory.getChangedPay())
                .finalPay(payHistory.getFinalPay())
                .createdAt(payHistory.getCreatedAt())
                .build();
    }
}
