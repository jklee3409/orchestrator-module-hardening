package eureca.capstone.project.orchestrator.market_statistics.service.impl;

import eureca.capstone.project.orchestrator.market_statistics.dto.CarrierPriceDto;
import eureca.capstone.project.orchestrator.market_statistics.dto.HourlyPriceStatDto;
import eureca.capstone.project.orchestrator.market_statistics.entity.MarketStatistic;
import eureca.capstone.project.orchestrator.market_statistics.repository.MarketStatisticsRepository;
import eureca.capstone.project.orchestrator.market_statistics.service.MarketStatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MarketStatisticServiceImpl implements MarketStatisticService {

    private final MarketStatisticsRepository marketStatisticsRepository;

    @Override
    public List<HourlyPriceStatDto> getHourlyPriceStats() {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.truncatedTo(ChronoUnit.HOURS);
        LocalDateTime startTime = endTime.minusHours(24);

        List<MarketStatistic> statistics = marketStatisticsRepository.findAllByStaticsTimeRange(startTime, endTime);

        Map<LocalDateTime, List<MarketStatistic>> marketStatsGroupedByTime = statistics.stream()
                .collect(Collectors.groupingBy(MarketStatistic::getStaticsTime));

        List<LocalDateTime> times = new ArrayList<>(24);
        for (int i = 0; i < 24; i++) {
            times.add(startTime.plusHours(i));
        }
        List<HourlyPriceStatDto> priceStatsDtoList = new ArrayList<>();

        for (LocalDateTime time : times) {

            List<CarrierPriceDto> pricesByCarrier =
                    marketStatsGroupedByTime.getOrDefault(time, Collections.emptyList())
                            .stream()
                            .map(stat -> CarrierPriceDto.builder()
                                    .carrierName(stat.getTelecomCompany().getName())
                                    .pricePerGb(stat.getAveragePrice())
                                    .build())
                            .toList();

            priceStatsDtoList.add(HourlyPriceStatDto.builder()
                    .date(time.toLocalDate().toString())
                    .hour(time.getHour())
                    .pricesByCarrier(pricesByCarrier)
                    .build());
        }

        return priceStatsDtoList;
    }
}
