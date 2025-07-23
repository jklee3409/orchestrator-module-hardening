package eureca.capstone.project.orchestrator.transaction_feed.repository;

import eureca.capstone.project.orchestrator.transaction_feed.document.TransactionFeedDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TransactionFeedSearchRepository extends ElasticsearchRepository<TransactionFeedDocument, Long> {
    @Query("update TransactionFeedDocument t set t.nickname = :nickname where t.sellerId = :sellerId")
    @Modifying
    void updateNicknameBySellerId(String nickname, Long sellerId);
}
