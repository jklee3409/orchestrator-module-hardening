package eureca.capstone.project.orchestrator.user.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateUserRequestDto {

    @NotNull(message = "통신사 정보는 필수입니다.")
    private Long telecomCompanyId;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, message = "닉네임은 2글자 이상이어야 합니다.")
    private String nickname;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password; // 비밀번호 (암호화된 값)

    @NotBlank(message = "전화번호는 필수입니다.")
    private String phoneNumber;

    private String provider;
}
