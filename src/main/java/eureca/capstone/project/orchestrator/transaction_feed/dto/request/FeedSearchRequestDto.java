package eureca.capstone.project.orchestrator.transaction_feed.dto.request;

import eureca.capstone.project.orchestrator.transaction_feed.dto.enums.FeedSort;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

@Data
@Builder
public class FeedSearchRequestDto {
    private String keyword;

    // 체크박스 필터 (다중 선택 가능)
    private List<Long> telecomCompanyIds;
    private List<Long> salesTypeIds;
    private List<String> statuses;

    // 범위 필터
    private Long minPrice;
    private Long maxPrice;
    private Long minDataAmount;
    private Long maxDataAmount;

    // 정렬
    @Builder.Default
    private FeedSort sortBy = FeedSort.LATEST; // 기본값: 최신순

    @Singular("excludeFeedId")
    private List<Long> excludeFeedIds;
}
