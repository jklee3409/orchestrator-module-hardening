package eureca.capstone.project.orchestrator.alarm.repository.custom;

import eureca.capstone.project.orchestrator.alarm.dto.NotificationDto;
import java.util.List;

public interface AlarmRepositoryCustom {
    List<NotificationDto> getAlarms(Long userId);
    void readAlarms(List<Long> alarmIds, Long userId);
}
