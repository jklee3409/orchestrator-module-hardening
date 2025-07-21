package eureca.capstone.project.orchestrator.user.dto;

import eureca.capstone.project.orchestrator.common.dto.TelecomCompanyDto;
import eureca.capstone.project.orchestrator.user.entity.Plan;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlanDto {
    private Long planId;
    private String planName;
    private Long monthlyDataMb;
    private TelecomCompanyDto telecomCompany;

    public static PlanDto fromEntity(Plan plan) {
        return PlanDto.builder()
                .planId(plan.getPlanId())
                .planName(plan.getPlanName())
                .monthlyDataMb(plan.getMonthlyDataMb())
                .telecomCompany(TelecomCompanyDto.fromEntity(plan.getTelecomCompany()))
                .build();
    }

    public static Plan toEntity(PlanDto planDto) {
        return Plan.builder()
                .planId(planDto.getPlanId())
                .planName(planDto.getPlanName())
                .monthlyDataMb(planDto.getMonthlyDataMb())
                .telecomCompany(TelecomCompanyDto.toEntity(planDto.getTelecomCompany()))
                .build();
    }
}
