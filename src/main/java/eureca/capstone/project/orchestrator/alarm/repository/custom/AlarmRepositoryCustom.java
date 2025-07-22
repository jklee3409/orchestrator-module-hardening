package eureca.capstone.project.orchestrator.alarm.repository.custom;

import eureca.capstone.project.orchestrator.alarm.dto.NotificationDto;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface AlarmRepositoryCustom {
    Slice<NotificationDto> getAlarms(Long userId, Pageable pageable);
    void readAlarms(List<Long> alarmIds, Long userId);
}
