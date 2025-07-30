package eureca.capstone.project.orchestrator.alarm.service.impl;

import eureca.capstone.project.orchestrator.alarm.dto.AlarmCreationDto;
import eureca.capstone.project.orchestrator.alarm.dto.NotificationDto;
import eureca.capstone.project.orchestrator.alarm.entity.Alarm;
import eureca.capstone.project.orchestrator.alarm.repository.AlarmRepository;
import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.common.exception.custom.UserNotFoundException;
import eureca.capstone.project.orchestrator.common.util.AlarmTypeManager;
import eureca.capstone.project.orchestrator.common.util.StatusManager;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final AlarmRepository alarmRepository;
    private final UserRepository userRepository;
    private final SseEmitterService sseEmitterService;
    private final AlarmTypeManager alarmTypeManager;
    private final StatusManager statusManager;

    @Transactional
    @KafkaListener(topics = "notification", groupId = "notification-group")
    public void consume(AlarmCreationDto creationDto) {
        log.info("[consume] 알림 생성 요청 수신: {}", creationDto.getContent());

        User user = findUserById(creationDto.getUserId());

        Status unReadStatus = statusManager.getStatus("ALARM", "UNREAD");

        Alarm alarm = Alarm.builder()
                .user(user)
                .content(creationDto.getContent())
                .alarmType(alarmTypeManager.getAlarmType(creationDto.getAlarmType()))
                .status(unReadStatus)
                .transactionFeedId(creationDto.getTransactionFeedId())
                .build();
        log.info("[consume] 판매글 ID: {}", creationDto.getTransactionFeedId());
        alarmRepository.save(alarm);
        log.info("[consume] 알림 저장 완료: 알림 ID={}, 사용자 ID={}", alarm.getAlarmId(), user.getUserId());

        NotificationDto notificationDto = NotificationDto.fromEntity(alarm);
        sseEmitterService.send(creationDto.getUserId(), notificationDto);
        log.info("[consume] SSE로 알림 전송 완료: 사용자 ID={}, 알림 ID={}", creationDto.getUserId(), alarm.getAlarmId());
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }
}
