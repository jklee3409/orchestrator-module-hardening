package eureca.capstone.project.orchestrator.market_statistics.controller;

import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.market_statistics.dto.HourlyPriceStatDto;
import eureca.capstone.project.orchestrator.market_statistics.service.MarketStatisticService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/orchestrator/statistic")
@RequiredArgsConstructor
public class MarketStatisticController {
    private final MarketStatisticService marketStatisticService;

    @Operation(summary = "시간대별 시세 통계 조회 API", description = """
            ## 최근 24시간 동안의 시간대별 데이터 시세를 조회합니다.
            각 시간대별로 통신사(SKT, KT, LGU+)의 1GB당 평균 시세 정보를 제공합니다.
            
            ***
            
            ### 📥 요청 파라미터
            * 요청 파라미터가 없습니다.
            
            ### 🔑 권한
            * 모든 사용자
            
            ### ❌ 주요 실패 코드
            * 이 API는 별도의 실패 코드를 반환하지 않습니다.
            
            ### 📝 참고 사항
            * 응답은 항상 **현재 시간 기준 과거 24시간**에 대한 24개의 시간별 데이터 목록을 포함합니다.
            * 특정 시간대에 특정 통신사의 거래 내역이 없는 경우, 해당 통신사에 대한 `CarrierPriceDto` 객체는 `pricesByCarrier` 목록에 포함되지 않습니다.
            * 만약 한 시간대에 모든 통신사의 거래 내역이 없다면, `pricesByCarrier`는 빈 리스트(`[]`)로 반환됩니다.
            """)
    @GetMapping
    public BaseResponseDto<List<HourlyPriceStatDto>> getStatistics() {
        List<HourlyPriceStatDto> response = marketStatisticService.getHourlyPriceStats();
        return BaseResponseDto.success(response);
    }
}