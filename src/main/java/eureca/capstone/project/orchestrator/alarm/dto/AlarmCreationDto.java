package eureca.capstone.project.orchestrator.alarm.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AlarmCreationDto {
    private Long userId;
    private String alarmType;
    private String content;
}
