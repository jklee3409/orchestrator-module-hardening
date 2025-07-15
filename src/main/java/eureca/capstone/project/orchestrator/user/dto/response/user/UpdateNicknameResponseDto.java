package eureca.capstone.project.orchestrator.user.dto.response.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateNicknameResponseDto {
    private Long userId;
    private String nickname;
}
