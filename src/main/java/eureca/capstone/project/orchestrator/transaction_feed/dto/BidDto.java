package eureca.capstone.project.orchestrator.transaction_feed.dto;

import eureca.capstone.project.orchestrator.transaction_feed.entity.Bids;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BidDto {
    private Long bidId;
    private String bidderNickname;
    private Long bidAmount;
    private LocalDateTime bidAt;

    public static BidDto fromEntity(Bids bid) {
        return BidDto.builder()
                .bidId(bid.getBidsId())
                .bidderNickname(bid.getUser().getNickname())
                .bidAmount(bid.getBidAmount())
                .bidAt(bid.getCreatedAt())
                .build();
    }
}
