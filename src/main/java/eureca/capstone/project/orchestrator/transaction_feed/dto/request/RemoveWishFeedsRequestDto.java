package eureca.capstone.project.orchestrator.transaction_feed.dto.request;

import java.util.List;
import lombok.Data;

@Data
public class RemoveWishFeedsRequestDto {
    private List<Long> transactionFeedIds;
}
