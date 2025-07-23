package eureca.capstone.project.orchestrator.market_statistics.service;

import eureca.capstone.project.orchestrator.market_statistics.dto.HourlyPriceStatDto;

import java.time.LocalDateTime;
import java.util.List;

public interface MarketStatisticService {
    List<HourlyPriceStatDto> getHourlyPriceStats();
}
