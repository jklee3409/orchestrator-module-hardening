package eureca.capstone.project.orchestrator.transaction_feed.dto.response;

import eureca.capstone.project.orchestrator.common.dto.StatusDto;
import eureca.capstone.project.orchestrator.common.dto.TelecomCompanyDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.SalesTypeDto;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetFeedDetailResponseDto {
    private final Long transactionFeedId;
    private final String title;
    private final String content;
    private final Long salesDataAmount;
    private final Long salesPrice;
    private final Long defaultImageNumber;
    private final LocalDateTime createdAt;
    private final String nickname;
    private final Long likedCount;
    private final TelecomCompanyDto telecomCompany;
    private final StatusDto status;
    private final SalesTypeDto salesType;
    private final LocalDateTime expiredAt;

    // 입찰 판매 시에만 사용 (Nullable)
    private final Long currentHeightPrice;
}
