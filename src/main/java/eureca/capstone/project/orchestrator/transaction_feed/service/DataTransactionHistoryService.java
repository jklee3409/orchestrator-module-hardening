package eureca.capstone.project.orchestrator.transaction_feed.service;

import eureca.capstone.project.orchestrator.transaction_feed.entity.DataTransactionHistory;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.user.entity.User;

public interface DataTransactionHistoryService {
    DataTransactionHistory createNormalTransactionHistory(User buyer, TransactionFeed transactionFeed);
    DataTransactionHistory createAuctionTransactionHistory(User buyer, TransactionFeed transactionFeed, Long finalPrice);
}
