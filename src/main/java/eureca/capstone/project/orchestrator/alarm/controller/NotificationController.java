package eureca.capstone.project.orchestrator.alarm.controller;

import eureca.capstone.project.orchestrator.alarm.dto.NotificationDto;
import eureca.capstone.project.orchestrator.alarm.dto.request.ReadNotificationsRequestDto;
import eureca.capstone.project.orchestrator.alarm.service.AlarmService;
import eureca.capstone.project.orchestrator.alarm.service.impl.SseEmitterService;
import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "알림 API", description = "사용자 알림 조회 및 실시간 알림 구독 등 API")
@Slf4j
@RestController
@RequestMapping("/orchestrator/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final SseEmitterService sseEmitterService;
    private final AlarmService alarmService;

    @Operation(summary = "알림 목록 조회 API", description = """
            ## 최근 14일간의 알림 목록을 페이징하여 조회합니다.
            
            ***
            
            ### 📥 요청 파라미터 (Query Parameters)
            | 이름 | 타입 | 필수 | 설명 | 기타 |
            |---|---|:---:|---|---|
            | `pageable` | `Object`| X | 페이지 정보 (`page`, `size`, `sort`) | 기본값: `size=20`, `sort=createdAt,DESC` |
            
            ### 🔑 권한
            * `ROLE_USER` (사용자 로그인 필요)
            
            ### ❌ 주요 실패 코드
            * `20000` (USER_NOT_FOUND): 유효하지 않은 토큰으로 요청하여 사용자를 찾을 수 없을 경우 발생합니다.
            
            ### 📝 참고 사항
            * 이 API는 `Slice` 객체를 반환하므로, 전체 아이템 수나 전체 페이지 수 정보는 포함하지 않습니다. 다음 페이지의 존재 여부(`hasNext`)만 확인할 수 있습니다.
            """)
    @GetMapping
    public BaseResponseDto<Slice<NotificationDto>> getNotifications(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Slice<NotificationDto> notifications = alarmService.getNotifications(customUserDetailsDto.getEmail(), pageable);
        return BaseResponseDto.success(notifications);
    }

    @Operation(summary = "실시간 알림 구독 (SSE) API", description = """
            ## 서버로부터 실시간 알림을 받기 위한 Server-Sent Events(SSE) 연결을 설정합니다.
            로그인 직후 호출하여 서버와의 연결을 시작해야 합니다.
            
            ***
            
            ### 📥 요청 파라미터
            * 별도의 요청 파라미터는 없으며, **Authorization 헤더의 토큰**으로 사용자를 식별합니다.
            
            ### 🔑 권한
            * `ROLE_USER` (사용자 로그인 필요)
            
            ### ❌ 주요 실패 코드
            * `401 Unauthorized`: 유효하지 않은 토큰으로 요청 시 연결이 거부됩니다.
            
            ### 📝 참고 사항
            * 이 API는 **`Content-Type: text/event-stream`**으로 응답하며, 연결이 유지되는 동안 서버에서 발생하는 새로운 알림을 실시간으로 클라이언트에 전송합니다.
            * 최초 연결 성공 시, `connect` 이벤트가 한 번 전송됩니다.
            * 연결 타임아웃은 2시간으로 설정되어 있으며, 타임아웃 발생 시 클라이언트에서 재연결을 시도해야 합니다.
            """)
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeNotifications(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto
    ) {
        log.info("[subscribeNotifications] SSE 컨트롤러 요청. userId: {}", customUserDetailsDto.getUserId());
        return sseEmitterService.subscribe(customUserDetailsDto.getUserId());
    }

    @Operation(summary = "알림 읽음 처리 API", description = """
            ## 하나 또는 여러 개의 알림을 '읽음' 상태로 변경합니다.
            
            ***
            
            ### 📥 요청 바디 (Request Body)
            ```json
            {
              "alarmIds": [1, 2, 3]
            }
            ```
            
            ### 🔑 권한
            * `ROLE_USER` (사용자 로그인 필요)
            
            ### ❌ 주요 실패 코드
            * `20000` (USER_NOT_FOUND): 유효하지 않은 토큰으로 요청할 경우 발생합니다.
            """)
    @PostMapping("/read")
    public BaseResponseDto<Void> readNotifications(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @RequestBody ReadNotificationsRequestDto requestDto
    ) {
        alarmService.readNotification(customUserDetailsDto.getEmail(), requestDto.getAlarmIds());
        return BaseResponseDto.voidSuccess();
    }
}