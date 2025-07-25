package eureca.capstone.project.orchestrator.pay.controller;

import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.pay.service.WebhookService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "토스 페이먼츠 웹훅 API", description = "토스 페이먼츠 웹훅을 수신하는 API")
@Slf4j
@RestController
@RequestMapping("/orchestrator/webhook")
@RequiredArgsConstructor
public class WebhookController {
    private final WebhookService webhookService;

    @PostMapping("/toss")
    public BaseResponseDto<Void> handelTossWebhook(@RequestBody Map<String, Object> payload) {
        log.info("[handelTossWebhook] 수신 시작: {}", payload);

        String eventType = (String) payload.get("eventType");
        if("PAYMENT_STATUS_CHANGED".equals(eventType)) {
            webhookService.processPaymentStatusChanged(payload);
            log.info("[handelTossWebhook] 결제 상태 변경 이벤트 처리 완료");
        }

        return BaseResponseDto.voidSuccess();
    }
}
