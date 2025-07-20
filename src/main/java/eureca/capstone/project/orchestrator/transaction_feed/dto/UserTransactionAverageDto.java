package eureca.capstone.project.orchestrator.transaction_feed.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTransactionAverageDto {
    private Double averagePrice;
    private Double averageDataAmount;
}
