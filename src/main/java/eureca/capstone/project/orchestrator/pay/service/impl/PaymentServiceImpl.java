package eureca.capstone.project.orchestrator.pay.service.impl;

import eureca.capstone.project.orchestrator.common.exception.custom.UserNotFoundException;
import eureca.capstone.project.orchestrator.common.util.PayTypeManager;
import eureca.capstone.project.orchestrator.common.util.StatusManager;
import eureca.capstone.project.orchestrator.pay.dto.PayTypeDto;
import eureca.capstone.project.orchestrator.pay.dto.request.CouponCalculationRequestDto;
import eureca.capstone.project.orchestrator.pay.dto.response.CouponCalculationResponseDto;
import eureca.capstone.project.orchestrator.pay.entity.PayType;
import eureca.capstone.project.orchestrator.pay.entity.UserEventCoupon;
import eureca.capstone.project.orchestrator.pay.repository.ChargeHistoryRepository;
import eureca.capstone.project.orchestrator.pay.repository.custom.UserEventCouponRepositoryCustom;
import eureca.capstone.project.orchestrator.pay.service.PaymentService;
import eureca.capstone.project.orchestrator.pay.service.UserEventCouponService;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
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

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }
}
