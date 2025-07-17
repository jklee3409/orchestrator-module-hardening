package eureca.capstone.project.orchestrator.pay.controller;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.pay.dto.request.CouponCalculationRequestDto;
import eureca.capstone.project.orchestrator.pay.dto.response.CouponCalculationResponseDto;
import eureca.capstone.project.orchestrator.pay.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orchestrator/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/event-coupon/calculate")
    @Operation(summary = "이벤트 쿠폰 적용시 할인 금액 조회 API", description = "userEventCouponId 에 해당하는 이벤트 쿠폰 사용 시 할인 금액과 결제 금액을 반환합니다.")
    public BaseResponseDto<CouponCalculationResponseDto> calculateDiscount(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @RequestBody CouponCalculationRequestDto requestDto
    ) {
        CouponCalculationResponseDto responseDto = paymentService.calculateDiscount(customUserDetailsDto.getEmail(), requestDto);
        return BaseResponseDto.success(responseDto);
    }
}
