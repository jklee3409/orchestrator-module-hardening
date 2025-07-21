package eureca.capstone.project.orchestrator.transaction_feed.dto.request;

import lombok.Data;

@Data
public class PlaceBidRequestDto {
    private Long transactionFeedId;
    private Long bidAmount;
}
