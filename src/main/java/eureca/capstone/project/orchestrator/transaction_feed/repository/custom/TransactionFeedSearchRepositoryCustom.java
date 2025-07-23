package eureca.capstone.project.orchestrator.transaction_feed.repository.custom;

public interface TransactionFeedSearchRepositoryCustom {
    void updateNicknameBySellerId(Long sellerId, String newNickname);
}
