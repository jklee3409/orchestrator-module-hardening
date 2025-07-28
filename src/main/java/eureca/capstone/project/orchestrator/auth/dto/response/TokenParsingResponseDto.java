package eureca.capstone.project.orchestrator.auth.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class TokenParsingResponseDto {
    private Long userId;
    private String email;
    private Set<String> roles;
    private Set<String> authorities;
}
