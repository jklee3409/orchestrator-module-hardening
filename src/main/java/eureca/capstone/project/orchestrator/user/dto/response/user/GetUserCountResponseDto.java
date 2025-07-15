package eureca.capstone.project.orchestrator.user.dto.response.user;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetUserCountResponseDto {
    private Long totalUserCount; // 전체 가입자 수
    private Long todayUserCount; // 당일 가입자 수
}
