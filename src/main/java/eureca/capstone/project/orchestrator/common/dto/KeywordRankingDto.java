package eureca.capstone.project.orchestrator.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class KeywordRankingDto {
    private String keyword;
    private int currentRank;
    private String trend;      // "UP", "DOWN", "SAME", "NEW"
    private Integer rankGap;   // 변동폭 (null이면 NEW)
}