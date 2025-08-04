package eureca.capstone.project.orchestrator.common.controller;

import eureca.capstone.project.orchestrator.common.dto.GetRankingResponseDto;
import eureca.capstone.project.orchestrator.common.dto.KeywordRankingDto;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.common.service.RedisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "실시간 검색어 API")
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


    @Operation(
            summary = "📊 검색어 랭킹·변동 조회 API",
            description = """
                    ## Redis에 저장된 인기 검색어 Top 10과 순위 변동 정보를 반환합니다.
                    
                    ***
                    
                    ### 🔁 반환 데이터 (배열 `List<KeywordRankingDto>`)
                    | 필드명        | 타입        | 설명                                                   |
                    |---------------|------------|--------------------------------------------------------|
                    | `keyword`     | `String`   | 검색어(소문자·공백 정제 완료)                          |
                    | `currentRank` | `Integer`  | 현재 순위 (1 ~ 10)                                     |
                    | `trend`       | `String`   | 순위 변화<br/>`NEW`, `UP`, `DOWN`, `SAME`              |
                    | `rankGap`     | `Integer`  | 순위 변화 폭<br/>(`UP` · `DOWN`일 때 양수, `SAME` → 0) |
                    
                    ### 📝 참고 사항
                    * 인기 검색어는 Redis **ZSET** 점수를 기준으로 실시간 집계합니다.
                    * `trend`가 `NEW`이면 이번 집계에서 처음 Top 10에 진입한 키워드입니다.
                    * 내부적으로 특수문자·제어문자는 제거된 상태로 저장·집계됩니다.
                    """
    )
    @GetMapping("/ranking")
    public BaseResponseDto<GetRankingResponseDto> getRanking() {
        GetRankingResponseDto trendingKeywords = redisService.getTopSearchKeywordsWithTrend(10);
        return BaseResponseDto.success(trendingKeywords);
    }
}
