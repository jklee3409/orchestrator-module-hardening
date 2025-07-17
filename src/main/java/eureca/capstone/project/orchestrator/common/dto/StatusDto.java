package eureca.capstone.project.orchestrator.common.dto;

import eureca.capstone.project.orchestrator.common.entity.Status;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatusDto {
    private Long statusId;
    private String code;

    public static StatusDto fromEntity(Status status) {
        return StatusDto.builder()
                .statusId(status.getStatusId())
                .code(status.getCode())
                .build();
    }
}
