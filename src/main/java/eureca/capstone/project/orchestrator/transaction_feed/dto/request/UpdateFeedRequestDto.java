package eureca.capstone.project.orchestrator.transaction_feed.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateFeedRequestDto {
    Long transactionFeedId;
    String title;
    String content;
    Long salesPrice;
    Long salesDataAmount;
    Long defaultImageNumber;
}
