package eureca.capstone.project.orchestrator.user.dto.response.user;

import eureca.capstone.project.orchestrator.common.dto.TelecomCompanyDto;
import eureca.capstone.project.orchestrator.user.entity.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetUserProfileResponseDto {
    private String nickname;
    private String email;
    private String phoneNumber;
    private TelecomCompanyDto telecomCompany;

    public static GetUserProfileResponseDto fromUser(User user) {
        return GetUserProfileResponseDto.builder()
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .telecomCompany(TelecomCompanyDto.fromEntity(user.getTelecomCompany()))
                .build();
    }
}
