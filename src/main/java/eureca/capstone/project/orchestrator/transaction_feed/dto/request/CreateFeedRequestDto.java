package eureca.capstone.project.orchestrator.transaction_feed.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateFeedRequestDto {
    String title;
    String content;
    Long TelecomCompanyId;
    Long SalesTypeId;
    Long salesPrice;
    Long salesDataAmount;
    Long defaultImageNumber;
}
