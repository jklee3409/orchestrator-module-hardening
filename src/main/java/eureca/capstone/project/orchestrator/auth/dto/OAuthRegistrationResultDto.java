package eureca.capstone.project.orchestrator.auth.dto;

import lombok.Data;

@Data
public class OAuthRegistrationResultDto {
    private final Long userId;
    private final boolean isNewUser;
}
