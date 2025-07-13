package eureca.capstone.project.orchestrator.dto.request.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateUserRequestDto {
    private String telecomCompany;
    private String email;
    private String nickname;
    private String password; // 비밀번호 (암호화된 값)
    private String phone;
    private String provider;
}
