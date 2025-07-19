package eureca.capstone.project.orchestrator.transaction_feed.repository;

import eureca.capstone.project.orchestrator.transaction_feed.document.TransactionFeedDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface TransactionFeedSearchRepository extends ElasticsearchRepository<TransactionFeedDocument, Long> {
}
