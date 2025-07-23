package eureca.capstone.project.orchestrator.market_statistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CarrierPriceDto {
    @Schema(description = "통신사명", example = "SKT / KT / LGU+")
    private String carrierName; // "SKT", "KT", "LGU+"

    @Schema(description = "1GB당 평균 시세(원). 해당시간대에 거래내역이 없을 경우 null 반환")
    private Long pricePerGb; // 1GB당 평균 시세
}
