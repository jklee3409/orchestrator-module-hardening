package eureca.capstone.project.orchestrator.user.dto.response.user_data;

import eureca.capstone.project.orchestrator.user.entity.UserData;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetUserDataStatusResponseDto {
    private Long totalDataMb;
    private Long sellableDataMb;
    private Long buyerDataMb;

    public static GetUserDataStatusResponseDto fromEntity(UserData userData) {
        return GetUserDataStatusResponseDto.builder()
                .totalDataMb(userData.getTotalDataMb())
                .sellableDataMb(userData.getSellableDataMb())
                .buyerDataMb(userData.getBuyerDataMb())
                .build();
    }
}
