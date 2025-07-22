package eureca.capstone.project.orchestrator.alarm.service;

import eureca.capstone.project.orchestrator.alarm.dto.NotificationDto;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface AlarmService {
    Slice<NotificationDto> getNotifications(String email, Pageable pageable);
    void readNotification(String email, List<Long> alarmIds);
}
