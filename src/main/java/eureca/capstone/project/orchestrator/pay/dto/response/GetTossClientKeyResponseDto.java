package eureca.capstone.project.orchestrator.pay.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetTossClientKeyResponseDto {
    private String clientKey;
}
