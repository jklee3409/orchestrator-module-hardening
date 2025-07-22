package eureca.capstone.project.orchestrator.transaction_feed.dto.response;

import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import java.time.LocalDateTime;
import java.util.Map;
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
    private String salesType;

    // 입찰 판매 시에만 사용 (Nullable)
    private final Long currentHeightPrice;

    public static GetFeedSummaryResponseDto fromEntity(TransactionFeed entity, boolean isLiked, Map<Long, Long> highestPriceMap) {
        Long currentHighestPrice = null;
        if (entity.getSalesType().getName().equals("입찰 판매")) {
            currentHighestPrice = highestPriceMap.getOrDefault(entity.getTransactionFeedId(), entity.getSalesPrice());
        }

        return GetFeedSummaryResponseDto.builder()
                .transactionFeedId(entity.getTransactionFeedId())
                .title(entity.getTitle())
                .nickname(entity.getUser().getNickname())
                .salesPrice(entity.getSalesPrice())
                .salesDataAmount(entity.getSalesDataAmount())
                .defaultImageNumber(entity.getDefaultImageNumber())
                .createdAt(entity.getCreatedAt())
                .liked(isLiked)
                .telecomCompany(entity.getTelecomCompany().getName())
                .status(entity.getStatus().getCode())
                .salesType(entity.getSalesType().getName())
                .currentHeightPrice(currentHighestPrice)
                .build();
    }
}
