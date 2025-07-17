package eureca.capstone.project.orchestrator.transaction_feed.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateFeedResponseDto {
    private Long transactionFeedId;
}
