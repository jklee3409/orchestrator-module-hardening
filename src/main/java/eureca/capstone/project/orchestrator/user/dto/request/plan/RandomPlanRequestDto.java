package eureca.capstone.project.orchestrator.user.dto.request.plan;

import eureca.capstone.project.orchestrator.common.entiry.TelecomCompany;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RandomPlanRequestDto {
    private TelecomCompany telecomCompany;
}
