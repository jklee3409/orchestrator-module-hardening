package eureca.capstone.project.orchestrator.transaction_feed.dto.response;

import eureca.capstone.project.orchestrator.transaction_feed.dto.BidDto;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetBidHistoryResponseDto {
    private List<BidDto> bids;
}
