package eureca.capstone.project.orchestrator.user.dto.request.user;

import lombok.Data;

@Data
public class UpdateUserTelecomAndPhoneRequestDto {
    private Long telecomCompanyId;
    private String phoneNumber;
}
