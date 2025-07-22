package eureca.capstone.project.orchestrator.alarm.dto;

import eureca.capstone.project.orchestrator.alarm.entity.AlarmType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AlarmTypeDto {
    private Long alarmTypeId;
    private String type;

    public static AlarmTypeDto fromEntity(AlarmType alarmType) {
        return AlarmTypeDto.builder()
                .alarmTypeId(alarmType.getAlarmTypeId())
                .type(alarmType.getType())
                .build();
    }
}
