package eureca.capstone.project.orchestrator.transaction_feed.dto.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort.Direction;

@Getter
@RequiredArgsConstructor
public enum FeedSort {
    LATEST("createdAt", Direction.DESC, "최신순"),
    PRICE_HIGH("salesPrice", Direction.DESC, "가격 높은 순"),
    PRICE_LOW("salesPrice", Direction.ASC, "가격 낮은 순");

    private final String property;
    private final Direction direction;
    private final String description;
}
