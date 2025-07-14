package eureca.capstone.project.orchestrator.dto.response.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateUserPasswordResponseDto {
    private Long userId;
}
