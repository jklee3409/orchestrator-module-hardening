package eureca.capstone.project.orchestrator.auth.dto.request;

import lombok.Data;

@Data
public class LoginRequestDto {
    private String email;
    private String password;
}
