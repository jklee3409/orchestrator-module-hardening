package eureca.capstone.project.orchestrator.dto.request.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CryptoPasswordRequestDto {
    private String password;
}
