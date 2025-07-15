package eureca.capstone.project.orchestrator.user.dto.response.user;

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

    @Data
    @Builder
    private static class TelecomCompanyDto {
        private Long telecomCompanyId;
        private String name;
    }

    public static GetUserProfileResponseDto fromUser(User user) {
        TelecomCompanyDto telecom = TelecomCompanyDto.builder()
                .telecomCompanyId(user.getTelecomCompany().getTelecomCompanyId())
                .name(user.getTelecomCompany().getName())
                .build();

        return GetUserProfileResponseDto.builder()
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .telecomCompany(telecom)
                .build();
    }
}
