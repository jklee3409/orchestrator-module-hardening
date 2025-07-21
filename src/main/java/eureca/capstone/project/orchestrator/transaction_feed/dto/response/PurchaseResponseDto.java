package eureca.capstone.project.orchestrator.transaction_feed.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PurchaseResponseDto {
    private Long transactionFeedId;
    private Long dataTransactionHistoryId;
    private Long price;
}
