package eureca.capstone.project.orchestrator.common.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GetRankingResponseDto {
    private String lastUpdatedAt;
    private List<KeywordRankingDto> top10;
}
