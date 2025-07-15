package eureca.capstone.project.orchestrator.user.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateNicknameRequestDto {
    @NotBlank(message = "사용자 ID는 필수입니다.")
    private Long userId;

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, message = "닉네임은 2글자 이상이어야 합니다.")
    private String nickname;
}
