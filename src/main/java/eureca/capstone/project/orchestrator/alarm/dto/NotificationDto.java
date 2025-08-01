package eureca.capstone.project.orchestrator.alarm.dto;

import eureca.capstone.project.orchestrator.alarm.entity.Alarm;
import eureca.capstone.project.orchestrator.common.dto.StatusDto;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationDto {
    private Long userId;
    private Long alarmId;
    private String content;
    private Long transactionFeedId;
    private AlarmTypeDto alarmType;
    private StatusDto status;
    private LocalDateTime createdAt;

    public static NotificationDto fromEntity(Alarm alarm) {
        return NotificationDto.builder()
                .userId(alarm.getUser().getUserId())
                .alarmId(alarm.getAlarmId())
                .content(alarm.getContent())
                .transactionFeedId(alarm.getTransactionFeedId())
                .alarmType(AlarmTypeDto.fromEntity(alarm.getAlarmType()))
                .status(StatusDto.fromEntity(alarm.getStatus()))
                .createdAt(alarm.getCreatedAt())
                .build();
    }
}
