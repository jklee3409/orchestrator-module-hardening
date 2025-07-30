package eureca.capstone.project.orchestrator.alarm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmCreationDto {
    private Long userId;
    private String alarmType;
    private String content;
    private Long transactionFeedId;
}
