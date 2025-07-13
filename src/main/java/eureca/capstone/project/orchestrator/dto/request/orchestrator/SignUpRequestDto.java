package eureca.capstone.project.orchestrator.dto.request.orchestrator;

import lombok.Data;

@Data
public class SignUpRequestDto {
    private String email;
    private String password;
    private String telecomCompany;
    private String phoneNumber;
}
