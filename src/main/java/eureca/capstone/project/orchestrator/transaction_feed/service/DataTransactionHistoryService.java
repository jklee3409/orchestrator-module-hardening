package eureca.capstone.project.orchestrator.transaction_feed.service;

import eureca.capstone.project.orchestrator.transaction_feed.dto.enums.TransactionHistoryType;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.GetTransactionHistoryResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.entity.DataTransactionHistory;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DataTransactionHistoryService {
    DataTransactionHistory createNormalTransactionHistory(User buyer, TransactionFeed transactionFeed);
    DataTransactionHistory createAuctionTransactionHistory(User buyer, TransactionFeed transactionFeed, Long finalPrice);
    Page<GetTransactionHistoryResponseDto> getTransactionHistory(String email, TransactionHistoryType type, Pageable pageable);
}
