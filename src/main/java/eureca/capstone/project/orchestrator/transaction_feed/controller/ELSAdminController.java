package eureca.capstone.project.orchestrator.transaction_feed.controller;

import eureca.capstone.project.orchestrator.transaction_feed.service.TransactionFeedService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/orchestrator/els/admin")
@RequiredArgsConstructor
public class ELSAdminController {
    private final TransactionFeedService transactionFeedService;

    @PostMapping("/reindex/feeds")
    @Operation(summary = "[디버깅용] Elasticsearch 재색인 API", description = "Elasticsearch 인덱스의 모든 문서를 재색인합니다.")
    public ResponseEntity<String> reindexAllFeeds() {
        log.info("[reindexAllFeeds] Elasticsearch 'transaction_feed' 인덱스 전체 재색인을 시작합니다.");
        try {
            long count = transactionFeedService.reindexAllFeeds();
            String message = String.format("재색인 성공. 총 %d개의 문서가 처리되었습니다.", count);
            log.info("[reindexAllFeeds] " + message);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            log.error("[reindexAllFeeds] 재색인 중 오류가 발생했습니다.", e);
            return ResponseEntity.internalServerError().body("재색인 실패: " + e.getMessage());
        }
    }
}
