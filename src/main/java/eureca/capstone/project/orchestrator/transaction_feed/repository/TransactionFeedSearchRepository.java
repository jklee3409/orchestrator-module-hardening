package eureca.capstone.project.orchestrator.transaction_feed.repository;

import eureca.capstone.project.orchestrator.transaction_feed.document.TransactionFeedDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.jpa.repository.Query;

public interface TransactionFeedSearchRepository extends ElasticsearchRepository<TransactionFeedDocument, Long> {
//    @Query("""
//    {
//      "script": {
//        "source": "ctx._source.nickname = params.newNickname",
//        "lang": "painless",
//        "params": {
//          "newNickname": "?0"
//        }
//      },
//      "query": {
//        "term": {
//          "sellerId": "?1"
//        }
//      }
//    }
//    """)
//    void updateNicknameBySellerId(String newNickname, Long sellerId);
}
