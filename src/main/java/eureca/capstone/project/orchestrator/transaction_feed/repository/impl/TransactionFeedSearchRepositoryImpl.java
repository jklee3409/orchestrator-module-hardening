package eureca.capstone.project.orchestrator.transaction_feed.repository.impl;

import eureca.capstone.project.orchestrator.transaction_feed.document.TransactionFeedDocument;
import eureca.capstone.project.orchestrator.transaction_feed.repository.custom.TransactionFeedSearchRepositoryCustom;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.ByQueryResponse;
import org.springframework.data.elasticsearch.core.query.ScriptType;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TransactionFeedSearchRepositoryImpl implements TransactionFeedSearchRepositoryCustom {
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public void updateNicknameBySellerId(Long sellerId, String newNickname) {
        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .term(t -> t
                                .field("sellerId")
                                .value(sellerId)
                        )
                )
                .build();

        UpdateQuery updateQuery = UpdateQuery.builder(searchQuery)
                .withScriptType(ScriptType.INLINE)
                .withLang("painless")
                .withScript("ctx._source.nickname = params.newNickname")
                .withParams(Collections.singletonMap("newNickname", newNickname))
                .build();

        ByQueryResponse response = elasticsearchOperations.updateByQuery(updateQuery,
                elasticsearchOperations.getIndexCoordinatesFor(TransactionFeedDocument.class));
    }
}
