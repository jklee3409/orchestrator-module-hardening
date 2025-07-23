package eureca.capstone.project.orchestrator.market_statistics.service.impl;

import eureca.capstone.project.orchestrator.market_statistics.dto.HourlyPriceStatDto;
import eureca.capstone.project.orchestrator.market_statistics.entity.MarketStatistic;
import eureca.capstone.project.orchestrator.market_statistics.repository.MarketStatisticsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarketStatisticServiceImplTest {

    @Mock
    private MarketStatisticsRepository marketStatisticsRepository;

    @InjectMocks
    private MarketStatisticServiceImpl marketStatisticService;

    @DisplayName("시세통계 조회_성공")
    @Test
    void getHourlyPriceStats_success() {

        // given
        LocalDateTime end = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);
        LocalDateTime last = end.minusHours(1);
        LocalDateTime prev = last.minusHours(1);

        MarketStatistic ms1 = Mockito.mock(MarketStatistic.class, Answers.RETURNS_DEEP_STUBS);
        when(ms1.getStaticsTime()).thenReturn(last);
        when(ms1.getTelecomCompany().getName()).thenReturn("SKT");
        when(ms1.getAveragePrice()).thenReturn(150L);

        MarketStatistic ms2 = Mockito.mock(MarketStatistic.class, Answers.RETURNS_DEEP_STUBS);
        when(ms2.getStaticsTime()).thenReturn(last);
        when(ms2.getTelecomCompany().getName()).thenReturn("KT");
        when(ms2.getAveragePrice()).thenReturn(145L);

        MarketStatistic ms3 = Mockito.mock(MarketStatistic.class, Answers.RETURNS_DEEP_STUBS);
        when(ms3.getStaticsTime()).thenReturn(prev);
        when(ms3.getTelecomCompany().getName()).thenReturn("SKT");
        when(ms3.getAveragePrice()).thenReturn(148L);

        MarketStatistic ms4 = Mockito.mock(MarketStatistic.class, Answers.RETURNS_DEEP_STUBS);
        when(ms4.getStaticsTime()).thenReturn(prev);
        when(ms4.getTelecomCompany().getName()).thenReturn("KT");
        when(ms4.getAveragePrice()).thenReturn(147L);

        when(marketStatisticsRepository.findAllByStaticsTimeRange(any(), any()))
                .thenReturn(List.of(ms1, ms2, ms3, ms4));

        // when
        List<HourlyPriceStatDto> result = marketStatisticService.getHourlyPriceStats();

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(24);

        ArgumentCaptor<LocalDateTime> fromCap = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> toCap   = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(marketStatisticsRepository).findAllByStaticsTimeRange(fromCap.capture(), toCap.capture());

        LocalDateTime start = fromCap.getValue();
        LocalDateTime capLast = toCap.getValue().minusHours(1);
        LocalDateTime capPrev = capLast.minusHours(1);

        // 기대 시간 순서 검증
        List<Integer> expectedHours = IntStream.range(0, 24)
                .map(i -> start.plusHours(i).getHour())
                .boxed().toList();
        List<Integer> actualHours = result.stream().map(HourlyPriceStatDto::getHour).toList();
        assertThat(actualHours).isEqualTo(expectedHours);

        Map<String, HourlyPriceStatDto> map = new HashMap<>();
        for (HourlyPriceStatDto d : result) {
            map.put(d.getDate() + "-" + d.getHour(), d);
        }

        String lastKey = capLast.toLocalDate() + "-" + capLast.getHour();
        String prevKey = capPrev.toLocalDate() + "-" + capPrev.getHour();

        HourlyPriceStatDto dtoLast = map.get(lastKey);
        HourlyPriceStatDto dtoPrev = map.get(prevKey);

        assertThat(dtoLast).isNotNull();
        assertThat(dtoPrev).isNotNull();
        assertThat(dtoLast.getPricesByCarrier()).hasSize(2);
        assertThat(dtoPrev.getPricesByCarrier()).hasSize(2);

        long emptyCnt = result.stream()
                .filter(d -> d.getPricesByCarrier().isEmpty())
                .count();
        assertThat(emptyCnt).isGreaterThanOrEqualTo(22);
    }

    @DisplayName("시세통계 조회_데이터 없음")
    @Test
    void getHourlyPriceStats_empty() {
        // given
        when(marketStatisticsRepository.findAllByStaticsTimeRange(any(), any()))
                .thenReturn(Collections.emptyList());

        // when
        List<HourlyPriceStatDto> result = marketStatisticService.getHourlyPriceStats();

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(24);
        assertThat(result).allMatch(d -> d.getPricesByCarrier().isEmpty());

        verify(marketStatisticsRepository).findAllByStaticsTimeRange(any(), any());
    }
}