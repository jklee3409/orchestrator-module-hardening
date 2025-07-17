package eureca.capstone.project.orchestrator.pay.service.impl;

import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.common.exception.code.ErrorCode;
import eureca.capstone.project.orchestrator.common.exception.custom.InternalServerException;
import eureca.capstone.project.orchestrator.common.exception.custom.UserNotFoundException;
import eureca.capstone.project.orchestrator.common.util.PayTypeManager;
import eureca.capstone.project.orchestrator.common.util.StatusManager;
import eureca.capstone.project.orchestrator.pay.dto.PayTypeDto;
import eureca.capstone.project.orchestrator.pay.dto.request.CouponCalculationRequestDto;
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
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
