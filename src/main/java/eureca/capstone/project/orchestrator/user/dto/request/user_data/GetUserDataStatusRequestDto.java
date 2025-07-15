package eureca.capstone.project.orchestrator.user.dto.request.user_data;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetUserDataStatusRequestDto {
    @NotNull(message = "사용자 ID 는 필수입니다.")
    private Long userId;
}
