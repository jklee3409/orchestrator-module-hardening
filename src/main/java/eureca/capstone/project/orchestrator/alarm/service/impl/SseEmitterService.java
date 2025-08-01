package eureca.capstone.project.orchestrator.alarm.service.impl;

import eureca.capstone.project.orchestrator.alarm.dto.NotificationDto;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
public class SseEmitterService {
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    // Heartbeat(ping) 전송을 위한 스케줄러
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public SseEmitter subscribe(Long userId) {
        log.info("[subscribe] SSE 연결 요청 서비스 레이어 도착. userId: {}", userId);

        SseEmitter emitter = new SseEmitter(2 * 60 * 60 * 1000L);
        emitters.put(userId, emitter);

        ScheduledFuture<?> heartbeat = scheduler.scheduleAtFixedRate(
                () -> sendPing(emitter, userId),
                30, 30, TimeUnit.SECONDS
        );

        emitter.onCompletion(() -> {
            log.info("[subscribe] SSE 연결 종료: userId={}", userId);
            heartbeat.cancel(false);
            emitters.remove(userId);
        });
        emitter.onTimeout(() -> {
            log.info("[subscribe] SSE 연결 타임아웃: userId={}", userId);
            heartbeat.cancel(false);
            emitters.remove(userId);
        });
        emitter.onError(e -> {
            log.error("[subscribe] SSE 연결 에러: userId={}, error={}", userId, e.getMessage());
            heartbeat.cancel(false);
            emitters.remove(userId);
        });

        log.info("[subscribe] SSE 연결 설정 완료");
        sendToClient(emitter, userId, "connect", Map.of("message", "SSE 연결 설정 완료"));
        log.info("[subscribe] SSE 연결 설정 메시지 전송 완료");
        return emitter;
    }

    public void send(Long userId, NotificationDto notificationDto) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            sendToClient(emitter, userId, "notification", notificationDto);
        } else {
            log.warn("[send] 알림을 보낼 Emitter를 찾을 수 없습니다. userId={}", userId);
        }
    }

    private void sendToClient(SseEmitter emitter, Long userId, String eventName, Object data) {
        log.info("[sendToClient] userId={}, eventName={}, data={}", userId, eventName, data);
        try {
            emitter.send(SseEmitter.event()
                    .id(String.valueOf(System.currentTimeMillis()))
                    .name(eventName)
                    .data(data)
            );
            log.info("[sendToClient] SSE 전송 성공: userId={}, eventName={}", userId, eventName);
        } catch (Exception e) {
            log.error("[sendToClient] SSE 전송 실패: userId={}, eventName={}, error={}", userId, eventName, e.getMessage());
            emitter.completeWithError(e);
        }
    }

    private void sendPing(SseEmitter emitter, Long userId) {
        try {
            emitter.send(SseEmitter.event().comment("ping"));
            log.info("[sendPing] Heartbeat 전송 성공: userId={}", userId);
        } catch (Exception e) {
            log.error("[sendPing] Heartbeat 전송 실패, 연결을 종료합니다. userId={}, error={}", userId, e.getMessage());
            emitter.completeWithError(e);
        }
    }
}