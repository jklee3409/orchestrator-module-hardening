package eureca.capstone.project.orchestrator.pay.service.impl;

import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.common.exception.code.ErrorCode;
import eureca.capstone.project.orchestrator.common.exception.custom.InternalServerException;
import eureca.capstone.project.orchestrator.common.exception.custom.UserNotFoundException;
import eureca.capstone.project.orchestrator.common.util.PayTypeManager;
import eureca.capstone.project.orchestrator.common.util.StatusManager;
import eureca.capstone.project.orchestrator.pay.dto.PayTypeDto;
import eureca.capstone.project.orchestrator.pay.dto.request.CouponCalculationRequestDto;
import eureca.capstone.project.orchestrator.pay.dto.request.PaymentApprovalRequestDto;
import eureca.capstone.project.orchestrator.pay.dto.request.PaymentPrepareRequestDto;
import eureca.capstone.project.orchestrator.pay.dto.response.CouponCalculationResponseDto;
import eureca.capstone.project.orchestrator.pay.dto.response.PaymentPrepareResponseDto;
import eureca.capstone.project.orchestrator.pay.entity.ChargeHistory;
import eureca.capstone.project.orchestrator.pay.entity.PayType;
import eureca.capstone.project.orchestrator.pay.entity.UserEventCoupon;
import eureca.capstone.project.orchestrator.pay.repository.ChargeHistoryRepository;
import eureca.capstone.project.orchestrator.pay.repository.custom.UserEventCouponRepositoryCustom;
import eureca.capstone.project.orchestrator.pay.service.PaymentService;
import eureca.capstone.project.orchestrator.pay.service.UserEventCouponService;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final UserEventCouponRepositoryCustom userEventCouponRepositoryCustom;
    private final UserEventCouponService userEventCouponService;
    private final ChargeHistoryRepository chargeHistoryRepository;
    private final UserRepository userRepository;
    private final StatusManager statusManager;
    private final PayTypeManager payTypeManager;
    private final WebClient.Builder webClientBuilder;

    @Value("${payment.toss.secret_key}")
    private String tossSecretKey;

    @Override
    @Transactional(readOnly = true)
    public CouponCalculationResponseDto calculateDiscount(String email, CouponCalculationRequestDto requestDto) {
        log.info("[calculateDiscount] 이벤트 쿠폰 할인 가격 계산 시작");

        User user = findUserByEmail(email);
        UserEventCoupon coupon = userEventCouponService.validateAndGetCoupon(requestDto.getUserEventCouponId(), user);
        log.info("[calculateDiscount] 사용자 및 이벤트 쿠폰 조회 완료");

        double discountRate = coupon.getEventCoupon().getDiscountRate() / 100.0;
        double calculateDiscount = requestDto.getOriginalAmount() * discountRate;

        Long discountAmount = Math.round(calculateDiscount);
        Long finalAmount = requestDto.getOriginalAmount() - discountAmount;
        log.info("[calculateDiscount] 할인 금액 및 최종 금액 계산 완료. 할인 금액: {}, 최종 금액: {}", discountAmount, finalAmount);

        PayType requiredPayType = payTypeManager.getPayType(coupon.getEventCoupon().getPayType().getName());
        log.info("[calculateDiscount] 쿠폰 적용을 위한 필요 결제 수단: {}", requiredPayType.getName());

        return CouponCalculationResponseDto.builder()
                .originalAmount(requestDto.getOriginalAmount())
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .requiredPayType(PayTypeDto.fromEntity(requiredPayType))
                .build();
    }

    @Override
    @Transactional
    public PaymentPrepareResponseDto preparePayment(String email, PaymentPrepareRequestDto requestDto) {
        log.info("[preparePayment] 결제 검증을 위한 주문 정보 생성 요청");

        User user = findUserByEmail(email);
        UserEventCoupon coupon = null;
        Long discountAmount = 0L;
        PayType requiredPayType = null;

        if (requestDto.getUserEventCouponId() != null) {
            log.info("[preparePayment] 이벤트 쿠폰 적용 요청");
            coupon = userEventCouponService.validateAndGetCoupon(requestDto.getUserEventCouponId(), user);

            double discountRate = coupon.getEventCoupon().getDiscountRate() / 100.0;
            double calculateDiscount = requestDto.getOriginalAmount() * discountRate;

            discountAmount = Math.round(calculateDiscount);
            log.info("[preparePayment] 할인 금액 계산 검증. 할인 금액: {}", discountAmount);

            requiredPayType = payTypeManager.getPayType(coupon.getEventCoupon().getPayType().getName());
            log.info("[preparePayment] 쿠폰 사용을 위한 결제 수단: {}", requiredPayType.getName());
        }

        Long expectedFinalAmount = requestDto.getOriginalAmount() - discountAmount;
        if (!expectedFinalAmount.equals(requestDto.getFinalAmount())) throw new InternalServerException(ErrorCode.FINAL_AMOUNT_NOT_MATCHED);
        log.info("[preparePayment] 요청 결제 금액과 일치");

        ChargeHistory chargeHistory = createRequestChargeHistory(requestDto.getOriginalAmount(), discountAmount, requestDto.getFinalAmount(), user, coupon);
        chargeHistoryRepository.save(chargeHistory);
        log.info("[preparePayment] 결제 전 주문 정보 생성 및 저장 완료");

        return new PaymentPrepareResponseDto(chargeHistory, requiredPayType);
    }

    @Override
    @Transactional
    public void confirmPayment(PaymentApprovalRequestDto requestDto) {
        log.info("[confirmPayment] 최종 결제 요청 및 승인 시작");
        ChargeHistory chargeHistory = chargeHistoryRepository.findByOrderIdWithDetails(requestDto.getOrderId())
                .orElseThrow(() -> new InternalServerException(ErrorCode.ORDER_NOT_FOUND));

        validatePayment(chargeHistory, requestDto.getAmount());
        log.info("[confirmPayment] 토스 결제 요청 금액과 주문 금액 일치 확인");

        String encodedSecretKey = Base64.getEncoder().encodeToString((tossSecretKey + ":").getBytes(StandardCharsets.UTF_8));
        log.info("[confirmPayment] 토스 시크릿 키 인코딩 완료");

        Map<String, Object> tossResponse = webClientBuilder.build().post()
                .uri("https://api.tosspayments.com/v1/payments/confirm")
                .header("Authorization", "Basic " + encodedSecretKey)
                .header("Idempotency-Key", requestDto.getOrderId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
        log.info("[confirmPayment] 토스에 최종 결제 승인 요청 전송 완료");

        String doneStatus = statusManager.getStatus("TOSS", "DONE").getCode();
        if (tossResponse != null && doneStatus.equals(tossResponse.get("status"))) {
            processPaymentSuccess(chargeHistory, requestDto.getPaymentKey());
            log.info("[confirmPayment] 토스 결제 승인 요청 응답 정상 확인. 최종 결제 성공 처리. paymentKey: {}", requestDto.getPaymentKey());

        } else {
            processPaymentFailed(chargeHistory);
            log.info("[confirmPayment] 토스 결제 승인 요청 응답 실패 확인. 최종 결제 실패 처리. paymentKey: {}", requestDto.getPaymentKey());
        }
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }

    private ChargeHistory createRequestChargeHistory(Long originalAmount, Long discountAmount, Long finalAmount, User user, UserEventCoupon coupon) {
        log.info("[createRequestChargeHistory] 충전 내역 생성 시작 - 결제 전 주문 정보만 생성");

        Status requestedStatus = statusManager.getStatus("PAYMENT", "REQUESTED");
        String orderId = UUID.randomUUID().toString();

        return ChargeHistory.builder()
                .user(user)
                .userEventCoupon(coupon)
                .orderId(orderId)
                .amount(originalAmount)
                .chargePay(finalAmount)
                .discountAmount(discountAmount)
                .status(requestedStatus)
                .requestedAt(LocalDateTime.now())
                .build();
    }

    private void validatePayment(ChargeHistory history, Long amountFromToss) {
        log.info("[validatePayment] 주문 정보와 토스 결제 금액 검증 시작");
        Status requestdStatus = statusManager.getStatus("PAYMENT", "REQUESTED");
        if (!requestdStatus.equals(history.getStatus())) throw new InternalServerException(ErrorCode.ORDER_ALREADY_PROCESSED);
        log.info("[validatePayment] 주문 상태 검증 완료");
        if (!history.getChargePay().equals(amountFromToss)) throw new InternalServerException(ErrorCode.FINAL_AMOUNT_NOT_MATCHED);
        log.info("[validatePayment] 주문 금액과 최종 토스 결제 요청 금액 검증 완료");
    }

    private void processPaymentSuccess(ChargeHistory history, String paymentKey) {
        log.info("[processPaymentSuccess] 충전 내역 결제 요청 성공으로 처리 시작. paymentKey: {}", paymentKey);

        Status completedStatus = statusManager.getStatus("PAYMENT", "COMPLETED");
        history.processSuccess(paymentKey, completedStatus);
        log.info("[processPaymentSuccess] 충전 내역 결제 상태 완료로 처리. 충전 내역 ID: {}", history.getChargeHistoryId());

        if (history.getUserEventCoupon() != null){
            userEventCouponService.useCoupon(history.getUserEventCoupon());
            log.info("[processPaymentSuccess] 사용자 이벤트 쿠폰 사용 처리 완료. 쿠폰 ID: {}", history.getUserEventCoupon().getUserEventCouponId());
        }

        // TODO: 사용자 페이 충전 메서드 구현
    }

    private void processPaymentFailed(ChargeHistory history) {
        log.info("[processPaymentFailed] 충전 내역 결제 요청 실패 처리 시작. 충전 내역 ID: {}", history.getChargeHistoryId());

        Status failedStatus = statusManager.getStatus("PAYMENT", "FAILED");
        history.processFailure(failedStatus);
        log.info("[processPaymentFailed] 충전 내역 결제 요청 실패 처리 완료. 충전 내역 ID: {}", history.getChargeHistoryId());
    }
}
