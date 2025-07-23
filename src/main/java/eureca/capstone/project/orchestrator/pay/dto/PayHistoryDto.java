package eureca.capstone.project.orchestrator.pay.dto;

import eureca.capstone.project.orchestrator.pay.entity.ChargeHistory;
import eureca.capstone.project.orchestrator.pay.entity.ExchangeHistory;
import eureca.capstone.project.orchestrator.pay.entity.PayHistory;
import eureca.capstone.project.orchestrator.transaction_feed.entity.DataTransactionHistory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

public class PayHistoryDto {

    @Data
    @Builder
    public static class PayHistoryResponseDto {
        private List<PayHistorySimpleDto> histories;

        public static PayHistoryResponseDto fromEntity(List<PayHistory> payHistories) {
            List<PayHistorySimpleDto> historyDtoList = payHistories.stream()
                    .map(PayHistorySimpleDto::fromEntity)
                    .toList();
            return new PayHistoryResponseDto(historyDtoList);
        }
    }

    @Data
    @Builder
    public static class PayHistorySimpleDto {
        private Long payHistoryId;
        private String changeType;
        private Long changePay;
        private LocalDateTime createdAt;

        public static PayHistorySimpleDto fromEntity(PayHistory payHistory) {
            return PayHistorySimpleDto.builder()
                .payHistoryId(payHistory.getPayHistoryId())
                .changeType(payHistory.getChangeType().getType())
                .changePay(payHistory.getChangedPay())
                .createdAt(payHistory.getCreatedAt())
                .build();
        }
    }
}
