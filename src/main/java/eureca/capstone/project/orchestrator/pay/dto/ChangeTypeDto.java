package eureca.capstone.project.orchestrator.pay.dto;

import eureca.capstone.project.orchestrator.pay.entity.ChangeType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChangeTypeDto {
    private Long changeTypeId;
    private String type;

    public static ChangeTypeDto fromEntity(ChangeType changeType) {
        return ChangeTypeDto.builder()
                .changeTypeId(changeType.getChangeTypeId())
                .type(changeType.getType())
                .build();
    }
}
