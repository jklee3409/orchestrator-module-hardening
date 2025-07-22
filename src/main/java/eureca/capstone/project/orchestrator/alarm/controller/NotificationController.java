package eureca.capstone.project.orchestrator.alarm.controller;

import eureca.capstone.project.orchestrator.alarm.dto.NotificationDto;
import eureca.capstone.project.orchestrator.alarm.dto.request.ReadNotificationsRequestDto;
import eureca.capstone.project.orchestrator.alarm.service.AlarmService;
import eureca.capstone.project.orchestrator.alarm.service.impl.SseEmitterService;
import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orchestrator/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final SseEmitterService sseEmitterService;
    private final AlarmService alarmService;

    @GetMapping
    @Operation(summary = "알림 조회 API", description = "최근 14일간의 알림을 조회합니다.")
    public BaseResponseDto<Slice<NotificationDto>> getNotifications(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @PageableDefault(size = 20, sort = "createdAt, desc") Pageable pageable
    ) {
        Slice<NotificationDto> notifications = alarmService.getNotifications(customUserDetailsDto.getEmail(), pageable);
        return BaseResponseDto.success(notifications);
    }

    @PostMapping("/read")
    @Operation(summary = "알림 읽음 처리 API", description = "선택한 알림을 읽음 처리합니다.")
    public BaseResponseDto<Void> readNotifications(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @RequestBody ReadNotificationsRequestDto requestDto
    ) {
        alarmService.readNotification(customUserDetailsDto.getEmail(), requestDto.getAlarmIds());
        return BaseResponseDto.voidSuccess();
    }
}
