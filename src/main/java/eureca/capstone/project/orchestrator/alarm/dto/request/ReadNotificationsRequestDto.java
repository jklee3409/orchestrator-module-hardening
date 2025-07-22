package eureca.capstone.project.orchestrator.alarm.dto.request;

import java.util.List;
import lombok.Data;

@Data
public class ReadNotificationsRequestDto {
    List<Long> alarmIds;
}
