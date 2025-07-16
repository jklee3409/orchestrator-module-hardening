package eureca.capstone.project.orchestrator.user.dto.response.user_data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeductSellableDataResponseDto {
    private Long userId;
    private Long sellableDataMb;
}
