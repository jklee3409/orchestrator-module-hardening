package eureca.capstone.project.orchestrator.transaction_feed.event;

import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.user.entity.User;

public record BidSucceededEvent(
        Long transactionFeedId,
        Long bidderUserId,
        String bidderNickname,
        String feedTitle,
        Long bidAmount
) {
    public static BidSucceededEvent of(TransactionFeed feed, User bidder, Long bidAmount) {
        return new BidSucceededEvent(
                feed.getTransactionFeedId(),
                bidder.getUserId(),
                bidder.getNickname(),
                feed.getTitle(),
                bidAmount
        );
    }
}