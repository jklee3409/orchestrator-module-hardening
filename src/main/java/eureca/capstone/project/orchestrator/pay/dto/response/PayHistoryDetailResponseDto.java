package eureca.capstone.project.orchestrator.pay.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import eureca.capstone.project.orchestrator.pay.entity.ChargeHistory;
import eureca.capstone.project.orchestrator.pay.entity.ExchangeHistory;
import eureca.capstone.project.orchestrator.transaction_feed.entity.DataTransactionHistory;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PayHistoryDetailResponseDto {
    private Long payHistoryId;
    private String changeType;
    private Long finalUserPay;
    private LocalDateTime createdAt;

    private ChargeDetailDto chargeDetail;
    private ExchangeDetailDto exchangeDetail;
    private TransactionDetailDto transactionDetail;


    @Getter
    @Builder
    public static class ChargeDetailDto {
        private String orderId;
        private Long paymentAmount;
        private Long discountAmount;
        private Long finalPaymentAmount;
        private Long chargedPay;
        private LocalDateTime chargedAt;
        private String payTypeName;

        public static ChargeDetailDto fromEntity(ChargeHistory chargeHistory) {
            return ChargeDetailDto.builder()
                    .orderId(chargeHistory.getOrderId())
                    .paymentAmount(chargeHistory.getAmount())
                    .discountAmount(chargeHistory.getDiscountAmount())
                    .finalPaymentAmount(chargeHistory.getFinalAmount())
                    .chargedPay(chargeHistory.getChargePay())
                    .chargedAt(chargeHistory.getCompletedAt())
                    .payTypeName(chargeHistory.getPayType() != null ? chargeHistory.getPayType().getName() : "정보없음")
                    .build();
        }
    }

    @Getter
    @Builder
    public static class ExchangeDetailDto {
        private Long exchangeHistoryId;
        private Long exchangeAmount;
        private Long fee;
        private Long finalExchangeAmount;
        private Long exchangedPay;
        private LocalDateTime exchangedAt;
        private String bankName;
        private String exchangeAccount;

        public static ExchangeDetailDto fromEntity(ExchangeHistory exchangeHistory, Long changedPay) {
            return ExchangeDetailDto.builder()
                    .exchangeHistoryId(exchangeHistory.getExchangeHistoryId())
                    .exchangeAmount(exchangeHistory.getAmount())
                    .fee(exchangeHistory.getFee())
                    .finalExchangeAmount(exchangeHistory.getFinalAmount())
                    .exchangedPay(changedPay)
                    .exchangedAt(exchangeHistory.getCreatedAt())
                    .bankName(exchangeHistory.getBank() != null ? exchangeHistory.getBank().getBankName() : "정보없음")
                    .exchangeAccount(exchangeHistory.getExchangeAccount())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class TransactionDetailDto {
        private Long transactionHistoryId;
        private String transactionType;
        private String dataTitle;
        private Long transactionPay;
        private LocalDateTime transactedAt;
        private String telecom;

        public static TransactionDetailDto fromEntity(DataTransactionHistory txHistory, Long changedPay) {
            return TransactionDetailDto.builder()
                    .transactionHistoryId(txHistory.getTransactionHistoryId())
                    .transactionType(txHistory.getTransactionFeed().getSalesType().getName())
                    .dataTitle(txHistory.getTransactionFeed().getTitle())
                    .transactionPay(changedPay)
                    .transactedAt(txHistory.getCreatedAt())
                    .telecom(txHistory.getTransactionFeed().getTelecomCompany().getName())
                    .build();
        }
    }
}
