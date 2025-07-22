package eureca.capstone.project.orchestrator.auth.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponseDto {
    private String accessToken;
    private boolean isNewUser;
}
