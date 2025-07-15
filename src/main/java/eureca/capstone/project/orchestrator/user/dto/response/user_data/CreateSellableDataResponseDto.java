package eureca.capstone.project.orchestrator.user.dto.response.user_data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateSellableDataResponseDto {
    private Long userId;
    private Integer totalDataMb; // 전환 후 총 보유 데이터
    private Integer sellableDataMb; // 전환 후 총 판매 가능한 데이터
}
