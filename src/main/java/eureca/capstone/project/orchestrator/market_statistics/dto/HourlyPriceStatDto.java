package eureca.capstone.project.orchestrator.market_statistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class HourlyPriceStatDto {
    @Schema(description = "날짜", example = "2025-07-16")
    private String date; // 날짜 (예: "2025-07-16")

    @Schema(description = "시간", example = "0 ~ 23")
    private int hour;    // 시간 (0 ~ 23)
    private List<CarrierPriceDto> pricesByCarrier;
}
