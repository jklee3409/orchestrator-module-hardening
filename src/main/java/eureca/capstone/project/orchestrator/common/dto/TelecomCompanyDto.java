package eureca.capstone.project.orchestrator.common.dto;

import eureca.capstone.project.orchestrator.common.entity.TelecomCompany;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TelecomCompanyDto {
    private Long telecomCompanyId;
    private String name;

    public static TelecomCompanyDto fromEntity(TelecomCompany telecomCompany) {
        return TelecomCompanyDto.builder()
                .telecomCompanyId(telecomCompany.getTelecomCompanyId())
                .name(telecomCompany.getName())
                .build();
    }
}
