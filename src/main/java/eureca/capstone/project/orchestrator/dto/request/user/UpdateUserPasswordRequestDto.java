package eureca.capstone.project.orchestrator.dto.request.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateUserPasswordRequestDto {
    private Long userId;
    private String password;
}
