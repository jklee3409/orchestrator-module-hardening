package eureca.capstone.project.orchestrator.transaction_feed.dto.response;

import eureca.capstone.project.orchestrator.transaction_feed.entity.DataTransactionHistory;
import eureca.capstone.project.orchestrator.user.entity.User;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetTransactionHistoryResponseDto {
    private final Long transactionFeedId;
    private final String title;
    private final String otherPartyNickname; // 거래 상대방 닉네임
    private final Long salesPrice;
    private final Long transactionFinalPrice;
    private final Long salesDataAmount;
    private final Long defaultImageNumber;
    private final LocalDateTime transactionDate;
    private final String telecomCompany;
    private final String salesType;
    private final String transactionType; // "구매" 또는 "판매"
    private final boolean liked;

    public static GetTransactionHistoryResponseDto fromEntity(DataTransactionHistory history, Long currentUserId, boolean isLiked) {
        User buyer = history.getUser();
        User seller = history.getTransactionFeed().getUser();

        boolean isBuyer = buyer.getUserId().equals(currentUserId);
        String transactionType = isBuyer ? "구매" : "판매";
        String otherPartyNickname = isBuyer ? seller.getNickname() : buyer.getNickname();

        return GetTransactionHistoryResponseDto.builder()
                .transactionFeedId(history.getTransactionFeed().getTransactionFeedId())
                .title(history.getTransactionFeed().getTitle())
                .otherPartyNickname(otherPartyNickname)
                .salesPrice(history.getTransactionFeed().getSalesPrice())
                .transactionFinalPrice(history.getTransactionFinalPrice())
                .salesDataAmount(history.getTransactionFeed().getSalesDataAmount())
                .defaultImageNumber(history.getTransactionFeed().getDefaultImageNumber())
                .transactionDate(history.getCreatedAt())
                .telecomCompany(history.getTransactionFeed().getTelecomCompany().getName())
                .salesType(history.getTransactionFeed().getSalesType().getName())
                .transactionType(transactionType)
                .liked(isLiked)
                .build();
    }
}
