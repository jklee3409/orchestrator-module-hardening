package eureca.capstone.project.orchestrator.alarm.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import eureca.capstone.project.orchestrator.alarm.dto.NotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisNotificationSubscriber implements MessageListener {
    private final ObjectMapper objectMapper;
    private final SseEmitterService sseEmitterService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String json = new String(message.getBody());
            NotificationDto dto = objectMapper.readValue(json, NotificationDto.class);
            log.info("[RedisSubscriber] 수신된 알림: {}", dto);
            sseEmitterService.send(dto.getUserId(), dto);
            log.info("[RedisSubscriber] SSE 전송 완료: userId={}", dto.getUserId());

        } catch (Exception e) {
            log.error("[RedisSubscriber] 메시지 처리 실패", e);
        }
    }
}
