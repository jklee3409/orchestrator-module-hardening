package eureca.capstone.project.orchestrator.alarm.service.impl;

import eureca.capstone.project.orchestrator.alarm.dto.NotificationDto;
import eureca.capstone.project.orchestrator.alarm.repository.AlarmRepository;
import eureca.capstone.project.orchestrator.alarm.service.AlarmService;
import eureca.capstone.project.orchestrator.common.exception.custom.UserNotFoundException;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmServiceImpl implements AlarmService {
    private final UserRepository userRepository;
    private final AlarmRepository alarmRepository;

    @Override
    public Slice<NotificationDto> getNotifications(String email, Pageable pageable) {
        User user = findUserByEmail(email);
        log.info("[getNotifications] 최근 14일 알림 조회. 사용자 ID: {}", user.getUserId());
        return alarmRepository.getAlarms(user.getUserId(), pageable);
    }

    @Override
    public void readNotification(String email, List<Long> alarmIds) {
        User user = findUserByEmail(email);
        log.info("[readNotification] 알림 읽음 처리. 사용자 ID: {}, 알림 ID 목록: {}", user.getUserId(), alarmIds);
        alarmRepository.readAlarms(alarmIds, user.getUserId());
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

    }
}
