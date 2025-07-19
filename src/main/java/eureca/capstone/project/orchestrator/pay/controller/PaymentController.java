package eureca.capstone.project.orchestrator.pay.controller;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.pay.dto.request.CouponCalculationRequestDto;
import eureca.capstone.project.orchestrator.pay.dto.request.PaymentApprovalRequestDto;
import eureca.capstone.project.orchestrator.pay.dto.request.PaymentPrepareRequestDto;
import eureca.capstone.project.orchestrator.pay.dto.response.CouponCalculationResponseDto;
import eureca.capstone.project.orchestrator.pay.dto.response.GetTossClientKeyResponseDto;
import eureca.capstone.project.orchestrator.pay.dto.response.PaymentPrepareResponseDto;
import eureca.capstone.project.orchestrator.pay.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orchestrator/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @Value("${payment.toss.client_key}")
    private String tossClientKey;

    @GetMapping("/client-key")
    @Operation(summary = "토스 클라이언트 키 조회", description = "토스 페이먼츠 결제창 호출을 위한 클라이언트 키를 반환합니다.")
    public BaseResponseDto<GetTossClientKeyResponseDto> getPaymentConfig() {
        GetTossClientKeyResponseDto responseDto = GetTossClientKeyResponseDto.builder()
                .clientKey(tossClientKey)
                .build();
        return BaseResponseDto.success(responseDto);
    }

    @PostMapping("/event-coupon/calculate")
    @Operation(summary = "이벤트 쿠폰 적용시 할인 금액 조회 API", description = "userEventCouponId 에 해당하는 이벤트 쿠폰 사용 시 할인 금액과 결제 금액을 반환합니다.")
    public BaseResponseDto<CouponCalculationResponseDto> calculateDiscount(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @RequestBody CouponCalculationRequestDto requestDto
    ) {
        CouponCalculationResponseDto responseDto = paymentService.calculateDiscount(customUserDetailsDto.getEmail(), requestDto);
        return BaseResponseDto.success(responseDto);
    }

    @PostMapping("/prepare")
    @Operation(summary = "결제 전 주문 정보 생성 API", description = "토스 페이먼츠에 최종 결제 요청을 위한 주문 정보를 생성합니다. 최종 결제 버튼 클릭 시점에 호출합니다.")
    public BaseResponseDto<PaymentPrepareResponseDto> preparePayment(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @RequestBody PaymentPrepareRequestDto requestDto
    ) {
        PaymentPrepareResponseDto responseDto = paymentService.preparePayment(customUserDetailsDto.getEmail(), requestDto);
        return BaseResponseDto.success(responseDto);
    }

    @PostMapping("/confirm")
    @Operation(summary = "최종 결제 승인 요청 API", description = "토스 페이먼츠에 최종 결제 승인 요청을 보냅니다. successUrl 로 리다이렉트 시 호출합니다.")
    public BaseResponseDto<Void> confirmPayment(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @RequestBody PaymentApprovalRequestDto requestDto
    ) {
        paymentService.confirmPayment(requestDto);
        return BaseResponseDto.voidSuccess();
    }
}
