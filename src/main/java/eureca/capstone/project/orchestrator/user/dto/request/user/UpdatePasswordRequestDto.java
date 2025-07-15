package eureca.capstone.project.orchestrator.user.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdatePasswordRequestDto {
    private Long userId;
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
}
