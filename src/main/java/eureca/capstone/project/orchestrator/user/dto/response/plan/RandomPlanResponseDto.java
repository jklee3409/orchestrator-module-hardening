package eureca.capstone.project.orchestrator.user.dto.response.plan;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RandomPlanResponseDto {
    private Long planId;
    private Long monthlyDataMb;
}
