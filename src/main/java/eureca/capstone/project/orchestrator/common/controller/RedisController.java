package eureca.capstone.project.orchestrator.common.controller;

import eureca.capstone.project.orchestrator.common.dto.GetRankingResponseDto;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.common.service.RedisService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/orchestrator/common")
public class RedisController {
    private final RedisService redisService;

    @Operation(summary = "🔍 검색어 랭킹 등록 API", description = """
            ## 사용자가 검색한 키워드를 Redis ZSET에 저장합니다.
            
            ***
            
            ### 📥 요청 파라미터 (Query Parameters)
            | 이름 | 타입 | 필수 | 설명 |
            |---|---|:---:|---|
            | `keyword` | `String` | ✅ | 사용자가 입력한 검색 키워드 |
            
            ### 📝 참고 사항
            * 키워드는 내부적으로 정제 처리되어 Redis에 누적 저장됩니다.
            * 같은 키워드는 검색할수록 점수가 올라가며 인기 순위에 반영됩니다.
            """)
    @GetMapping("/execute")
    public BaseResponseDto<Void> execute(@RequestParam String keyword) {
        // 실제 검색 로직 대신 랭킹 저장만 처리
        redisService.increaseSearchKeyword(keyword);
        log.info("검색 키워드 등록 완료: " + keyword);
        BaseResponseDto<Void> success = BaseResponseDto.voidSuccess();
        log.info("[execute] : {}", success);
        return success;
    }


    @Operation(summary = "📊 검색어 랭킹 조회 API", description = """
            ## Redis에 저장된 인기 검색어 Top 10을 반환합니다.
            
            ***
            
            ### 🔁 반환 데이터
            | 필드명 | 타입 | 설명 |
            |--------|------|------|
            | `top10` | `List<String>` | 점수순으로 정렬된 인기 검색어 목록 |
            
            ### 📝 참고 사항
            * 인기 검색어는 Redis ZSET을 기반으로 실시간으로 누적되며, 점수가 높은 순서로 정렬됩니다.
            * 내부적으로 특수문자나 제어문자는 자동 정제되어 저장됩니다.
            """)
    @GetMapping("/ranking")
    public BaseResponseDto<GetRankingResponseDto> getRanking() {
        List<Object> top10 = redisService.getTopSearchKeywords(10);
        GetRankingResponseDto getRankingResponseDto = GetRankingResponseDto.builder()
                .top10(top10)
                .build();
        log.info("[getRanking] : {}", getRankingResponseDto);
        BaseResponseDto<GetRankingResponseDto> success = BaseResponseDto.success(getRankingResponseDto);
        log.info("[getRanking] success : {}", success);
        return success;
    }
}
