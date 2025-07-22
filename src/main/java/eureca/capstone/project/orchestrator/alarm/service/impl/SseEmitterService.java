package eureca.capstone.project.orchestrator.alarm.service.impl;

import eureca.capstone.project.orchestrator.alarm.dto.NotificationDto;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
public class SseEmitterService {
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 120; // 2 hour
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> {
            log.info("[subscribe] SSE 연결 종료: userId={}", userId);
            emitters.remove(userId);
        });
        emitter.onTimeout(() -> {
            log.info("[subscribe] SSE 연결 타임아웃: userId={}", userId);
            emitters.remove(userId);
        });
        emitter.onError(e -> {
            log.error("[subscribe] SSE 연결 에러: userId={}, error={}", userId, e.getMessage());
            emitters.remove(userId);
        });

        sendToClient(userId, "connect", Map.of("message", "SSE 연결 설정 완료"));
        return emitter;
    }

    public void send(Long userId, NotificationDto notificationDto) {
        sendToClient(userId, "notification", notificationDto);
    }

    private void sendToClient(Long userId, String eventName, Object data) {
        SseEmitter emitter = emitters.get(userId);

        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .id(String.valueOf(System.currentTimeMillis()))
                        .name(eventName)
                        .data(data)
                );
                log.info("[sendToClient] userId={}, eventName={}, data={}", userId, eventName, data);
            }catch (Exception e) {
                log.error("[sendToClient] SSE 전송 실패: userId={}, eventName={}, error={}", userId, eventName, e.getMessage());
                emitters.remove(userId);
            }
        }
    }
}
