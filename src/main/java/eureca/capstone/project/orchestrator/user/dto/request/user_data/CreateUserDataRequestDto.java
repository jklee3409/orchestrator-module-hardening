package eureca.capstone.project.orchestrator.user.dto.request.user_data;

import eureca.capstone.project.orchestrator.user.entity.UserData;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateUserDataRequestDto {
    private Long userId;
    private Long planId;
    private Integer monthlyDataMb;
    private Integer resetDataAt;

    public static UserData toEntity(CreateUserDataRequestDto requestDto) {
        return UserData.builder()
                .userId(requestDto.getUserId())
                .planId(requestDto.getPlanId())
                .totalDataMb(requestDto.getMonthlyDataMb())
                .sellableDataMb(0)
                .buyerDataMb(0)
                .resetDataAt(requestDto.getResetDataAt())
                .build();
    }
}
