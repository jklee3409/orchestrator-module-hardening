package eureca.capstone.project.orchestrator.transaction_feed.dto.response;

import eureca.capstone.project.orchestrator.common.dto.StatusDto;
import eureca.capstone.project.orchestrator.common.dto.TelecomCompanyDto;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetFeedSummaryResponseDto {
    private Long transactionFeedId;
    private String title;
    private String nickname;
    private Long salesPrice;
    private Long salesDataAmount;
    private Long defaultImageNumber;
    private LocalDateTime createdAt;
    private boolean liked;
    private String telecomCompany;
    private String status;

    // 입찰 판매 시에만 사용 (Nullable)
    private final Long currentHeightPrice;
}
