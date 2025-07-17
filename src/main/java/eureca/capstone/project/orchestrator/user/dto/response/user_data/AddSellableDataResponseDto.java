package eureca.capstone.project.orchestrator.user.dto.response.user_data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddSellableDataResponseDto {
    private Long userId;
    private Long sellableDataMb;
}
