package eureca.capstone.project.orchestrator.user.dto.response.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateUserTelecomAndPhoneResponseDto {
    private Long userId;
    private Long telecomCompanyId;
    private String phoneNumber;
}
