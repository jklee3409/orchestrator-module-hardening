package eureca.capstone.project.orchestrator.transaction_feed.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlaceBidRequestDto {
    private Long transactionFeedId;
    private Long bidAmount;
}
