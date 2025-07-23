package eureca.capstone.project.orchestrator.market_statistics.controller;

import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.market_statistics.dto.HourlyPriceStatDto;
import eureca.capstone.project.orchestrator.market_statistics.entity.MarketStatistic;
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

    @GetMapping
    @Operation(summary = "시세 통계 조회", description = "최근 24시간 통신사 별 시세통계를 조회하는 api입니다.")

    public BaseResponseDto<List<HourlyPriceStatDto>> getStatistics() {
        List<HourlyPriceStatDto> response = marketStatisticService.getHourlyPriceStats();
        return BaseResponseDto.success(response);
    }
}
