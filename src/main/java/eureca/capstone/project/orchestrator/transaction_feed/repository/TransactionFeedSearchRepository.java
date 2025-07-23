package eureca.capstone.project.orchestrator.transaction_feed.repository;

import eureca.capstone.project.orchestrator.transaction_feed.document.TransactionFeedDocument;
import eureca.capstone.project.orchestrator.transaction_feed.repository.custom.TransactionFeedSearchRepositoryCustom;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface TransactionFeedSearchRepository extends ElasticsearchRepository<TransactionFeedDocument, Long>,
        TransactionFeedSearchRepositoryCustom {
}
