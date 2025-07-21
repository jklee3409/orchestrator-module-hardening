package eureca.capstone.project.orchestrator.transaction_feed.service;

import eureca.capstone.project.orchestrator.transaction_feed.dto.response.PurchaseResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.user.entity.User;

public interface DataFeedPurchaseService {
    PurchaseResponseDto purchase(String buyerEmail, Long transactionFeedId);
    PurchaseResponseDto purchaseAuction(User buyer, TransactionFeed feed, Long finalBidAmount);
}
