package eureca.capstone.project.orchestrator.transaction_feed.controller;

import eureca.capstone.project.orchestrator.transaction_feed.service.TransactionFeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "관리자용 ELS API", description = "Elasticsearch 관련 관리자 및 디버깅용 API")
@Slf4j
@RestController
@RequestMapping("/orchestrator/els/admin")
@RequiredArgsConstructor
public class ELSAdminController {
    private final TransactionFeedService transactionFeedService;

    @Operation(summary = "[관리자/디버깅용] Elasticsearch 판매글 재색인 API", description = """
            ## ⚠️ 주의: 이 API는 디버깅 및 데이터 동기화 목적으로만 사용해야 합니다.
            ### 운영 환경에서 무분별하게 호출할 경우 Elasticsearch 클러스터에 심각한 부하를 유발할 수 있습니다.
            
            DB의 모든 판매글 데이터를 Elasticsearch 인덱스로 다시 가져와 전체 문서를 재구성(삭제 후 재생성)합니다.
            
            ***
            
            ### 📥 요청 파라미터
            * 요청 파라미터가 없습니다.
            
            ### ❌ 주요 실패 코드
            * `500 Internal Server Error`: 재색인 과정 중 DB 또는 Elasticsearch에서 오류 발생 시
            
            ### 📝 참고 사항
            * 이 기능은 데이터베이스와 Elasticsearch 검색 데이터 간의 정합성이 맞지 않을 때 동기화를 위해 사용됩니다.
            """)
    @PostMapping("/reindex/feeds")
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