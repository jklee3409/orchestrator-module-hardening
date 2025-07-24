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

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "결제 API", description = "토스 페이먼츠 연동 결제 관련 API")
@RestController
@RequestMapping("/orchestrator/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @Value("${payment.toss.client_key}")
    private String tossClientKey;

    @GetMapping("/client-key")
    @Operation(summary = "토스 클라이언트 키 조회", description = """
            ## 토스 페이먼츠 결제창 연동에 필요한 클라이언트 키를 조회합니다.
            
            ***
            
            ### 🔑 권한
            * `ROLE_USER`(사용자 로그인 필요)
            """)
    public BaseResponseDto<GetTossClientKeyResponseDto> getPaymentConfig() {
        GetTossClientKeyResponseDto responseDto = GetTossClientKeyResponseDto.builder()
                .clientKey(tossClientKey)
                .build();
        return BaseResponseDto.success(responseDto);
    }

    @PostMapping("/event-coupon/calculate")
    @Operation(summary = "쿠폰 적용 시 할인액 계산", description = """
            ## 결제 전, 특정 쿠폰을 적용했을 때의 예상 할인액과 최종 결제 금액을 미리 계산하여 반환합니다.
            
            ***
            
            ### 📥 요청 바디 (Request Body)
            ```json
            {
              "userEventCouponId": 1,
              "originalAmount": 50000
            }
            ```
            
            ### 📥 요청 바디 필드 설명
            * `userEventCouponId`: 적용할 사용자 쿠폰의 ID (숫자)
            * `originalAmount`: 할인이 적용되기 전의 원본 금액 (숫자)
            
            ### 🔑 권한
            * `ROLE_USER` (사용자 로그인 필요)
            
            ### ❌ 주요 실패 코드
            * `20000` (USER_NOT_FOUND): 사용자를 찾을 수 없는 경우
            * `40022` (USER_EVENT_COUPON_NOT_FOUND): 존재하지 않는 쿠폰 ID인 경우
            * `40020` (USER_EVENT_COUPON_NOT_MATCHED): 쿠폰 소유주가 아닌 경우
            * `40021` (USER_EVENT_COUPON_EXPIRED): 이미 사용했거나 만료된 쿠폰인 경우
            """)
    public BaseResponseDto<CouponCalculationResponseDto> calculateDiscount(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @RequestBody CouponCalculationRequestDto requestDto
    ) {
        CouponCalculationResponseDto responseDto = paymentService.calculateDiscount(customUserDetailsDto.getEmail(), requestDto);
        return BaseResponseDto.success(responseDto);
    }

    @PostMapping("/prepare")
    @Operation(summary = "결제 정보 사전 생성", description = """
            ## 토스 페이먼츠에 최종 결제 승인 요청을 보내기 전, 서버에 주문 정보를 미리 생성합니다.
            이 API 호출 성공 시 반환되는 `orderId`를 최종 결제 승인 시 사용해야 합니다.
            
            ***
            
            ### 📥 요청 바디 (Request Body)
            ```json
            {
              "userEventCouponId": 1,
              "originalAmount": 50000,
              "finalAmount": 45000
            }
            ```
            
            ### 📥 요청 바디 필드 설명
            * `userEventCouponId`: (선택) 사용할 쿠폰 ID (숫자)
            * `originalAmount`: 원본 금액 (숫자)
            * `finalAmount`: 할인이 적용된 최종 결제 금액 (숫자)
            
            ### 🔑 권한
            * `ROLE_USER` (사용자 로그인 필요)
            
            ### ❌ 주요 실패 코드
            * `20000` (USER_NOT_FOUND): 사용자를 찾을 수 없는 경우
            * `40052` (FINAL_AMOUNT_NOT_MATCHED): 서버에서 계산한 최종 금액과 요청된 최종 금액이 다른 경우
            * (쿠폰 사용 시) `/event-coupon/calculate` API의 모든 실패 코드 포함
            """)
    public BaseResponseDto<PaymentPrepareResponseDto> preparePayment(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @RequestBody PaymentPrepareRequestDto requestDto
    ) {
        PaymentPrepareResponseDto responseDto = paymentService.preparePayment(customUserDetailsDto.getEmail(), requestDto);
        return BaseResponseDto.success(responseDto);
    }

    @PostMapping("/confirm")
    @Operation(summary = "최종 결제 승인", description = """
    ## 토스 페이먼츠 결제창에서 성공적으로 인증 후, 리다이렉트된 페이지에서 최종 결제 승인을 요청합니다.
    이 API는 서버가 토스 페이먼츠 서버와 직접 통신하여 결제를 완료시킵니다.
    
    ***
    
    ### 📥 요청 바디 (Request Body)
    ```json
    {
      "orderId": "c8e7b17e-1b05-4233-827c-c76846b07d61",
      "paymentKey": "toss_payment_key_example",
      "amount": 45000
    }
    ```
    
    ### 📥 요청 바디 필드 설명
    * `orderId`: `/prepare` API에서 반환받은 주문 ID (문자열)
    * `paymentKey`: 토스 결제창에서 발급한 결제 키 (문자열)
    * `amount`: 최종 결제 금액 (숫자)
    
    ### 🔑 권한
    * `ROLE_USER` (사용자 로그인 필요)
    
    ### ❌ 주요 실패 코드
    * `40053` (ORDER_NOT_FOUND): 유효하지 않은 주문 ID인 경우
    * `40054` (ORDER_ALREADY_PROCESSED): 이미 처리된 주문인 경우
    * `40052` (FINAL_AMOUNT_NOT_MATCHED): `/prepare` 시점의 금액과 최종 승인 금액이 다른 경우
    * `40056` (PAYMENT_CANCELLED_BY_PAY_METHOD): 쿠폰 조건(결제수단)과 실제 결제수단이 달라 승인이 자동 취소된 경우
    """)
    public BaseResponseDto<Void> confirmPayment(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @RequestBody PaymentApprovalRequestDto requestDto
    ) {
        paymentService.confirmPayment(requestDto);
        return BaseResponseDto.voidSuccess();
    }
}
