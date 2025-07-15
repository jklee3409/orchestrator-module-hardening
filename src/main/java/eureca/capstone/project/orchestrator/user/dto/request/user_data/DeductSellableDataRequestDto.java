package eureca.capstone.project.orchestrator.user.dto.request.user_data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeductSellableDataRequestDto {
    private Long userId;
    private Integer amount;
}
