package eureca.capstone.project.orchestrator.transaction_feed.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateFeedRequestDto {
    String title;
    String content;
    Long telecomCompanyId;
    Long salesTypeId;
    Long salesPrice;
    Long salesDataAmount;
    Long defaultImageNumber;
}
