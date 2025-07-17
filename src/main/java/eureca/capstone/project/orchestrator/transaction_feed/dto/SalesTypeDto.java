package eureca.capstone.project.orchestrator.transaction_feed.dto;

import eureca.capstone.project.orchestrator.transaction_feed.entity.SalesType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SalesTypeDto {
    private Long salesTypeId;
    private String name;

    public static SalesTypeDto fromEntity(SalesType salesType) {
        return SalesTypeDto.builder()
                .salesTypeId(salesType.getSalesTypeId())
                .name(salesType.getName())
                .build();
    }
}
