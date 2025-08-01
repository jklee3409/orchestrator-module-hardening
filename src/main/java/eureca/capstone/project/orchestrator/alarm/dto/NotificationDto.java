package eureca.capstone.project.orchestrator.alarm.dto;

import eureca.capstone.project.orchestrator.alarm.entity.Alarm;
import eureca.capstone.project.orchestrator.common.dto.StatusDto;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationDto {
    private Long alarmId;
    private String content;
    private Long transactionFeedId;
    private AlarmTypeDto alarmType;
    private String salesType;
    private StatusDto status;
    private LocalDateTime createdAt;

    public static NotificationDto fromEntity(Alarm alarm, TransactionFeed transactionFeed) {
        String salesTypeName = null;
        if (transactionFeed != null && transactionFeed.getSalesType() != null) {
            salesTypeName = transactionFeed.getSalesType().getName();
        }
        
        return NotificationDto.builder()
                .alarmId(alarm.getAlarmId())
                .content(alarm.getContent())
                .transactionFeedId(alarm.getTransactionFeedId())
                .alarmType(AlarmTypeDto.fromEntity(alarm.getAlarmType()))
                .salesType(salesTypeName)
                .status(StatusDto.fromEntity(alarm.getStatus()))
                .createdAt(alarm.getCreatedAt())
                .build();
    }
}
