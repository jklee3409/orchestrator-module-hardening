package eureca.capstone.project.orchestrator.transaction_feed.service.impl;

import eureca.capstone.project.orchestrator.transaction_feed.entity.DataTransactionHistory;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.transaction_feed.repository.DataTransactionHistoryRepository;
import eureca.capstone.project.orchestrator.transaction_feed.service.DataTransactionHistoryService;
import eureca.capstone.project.orchestrator.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataTransactionHistoryServiceImpl implements DataTransactionHistoryService {
    private final DataTransactionHistoryRepository dataTransactionHistoryRepository;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public DataTransactionHistory createNormalTransactionHistory(User buyer, TransactionFeed transactionFeed) {
        log.info("[createTransactionHistory] 데이터 거래 내역 생성 시작. 구매자 ID: {}, 판매글 ID: {}", buyer.getUserId(), transactionFeed.getTransactionFeedId());

        DataTransactionHistory transactionHistory = DataTransactionHistory.builder()
                .user(buyer)
                .transactionFeed(transactionFeed)
                .transactionFinalPrice(transactionFeed.getSalesPrice())
                .isDeleted(false)
                .build();
        return dataTransactionHistoryRepository.save(transactionHistory);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public DataTransactionHistory createAuctionTransactionHistory(User buyer, TransactionFeed transactionFeed,
                                                                  Long finalPrice) {
        log.info("[createHistory] 경매 낙찰 거래 내역 생성. 구매자 ID: {}, 판매글 ID: {}, 최종가: {}", buyer.getUserId(), transactionFeed.getTransactionFeedId(), finalPrice);

        DataTransactionHistory newHistory = DataTransactionHistory.builder()
                .user(buyer)
                .transactionFeed(transactionFeed)
                .transactionFinalPrice(finalPrice)
                .isDeleted(false)
                .build();
        return dataTransactionHistoryRepository.save(newHistory);
    }
}
