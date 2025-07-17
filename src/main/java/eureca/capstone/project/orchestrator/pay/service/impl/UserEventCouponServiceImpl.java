package eureca.capstone.project.orchestrator.pay.service.impl;

import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.common.exception.custom.UserNotFoundException;
import eureca.capstone.project.orchestrator.common.util.StatusManager;
import eureca.capstone.project.orchestrator.pay.dto.UserEventCouponDto;
import eureca.capstone.project.orchestrator.pay.dto.response.GetUserEventCouponListResponseDto;
import eureca.capstone.project.orchestrator.pay.entity.UserEventCoupon;
import eureca.capstone.project.orchestrator.pay.repository.custom.UserEventCouponRepositoryCustom;
import eureca.capstone.project.orchestrator.pay.service.UserEventCouponService;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventCouponServiceImpl implements UserEventCouponService {
    private final UserEventCouponRepositoryCustom userEventCouponRepositoryCustom;
    private final UserRepository userRepository;
    private final StatusManager statusManager;

    @Override
    public GetUserEventCouponListResponseDto getAvailableCoupons(String email) {
        log.info("[getAvailableCoupons] 사용자 {} 의 사용 가능한 이벤트 쿠폰 목록 조회 시작", email);

        User user = findUserByEmail(email);
        Status issuedStatus = statusManager.getStatus("COUPON", "ISSUED");
        log.info("[getAvailableCoupons] 사용자 및 상태 조회 완료");

        List<UserEventCoupon> userEventCoupons = userEventCouponRepositoryCustom.findAvailableCouponsByUserAndStatus(user, issuedStatus);
        log.info("[getAvailableCoupons] 사용자 이벤트 쿠폰 목록 조회 완료");

        List<UserEventCouponDto> coupons = userEventCoupons.stream()
                .map(UserEventCouponDto::fromEntity)
                .toList();
        log.info("[getAvailableCoupons] UserEventCoupon ");

        return GetUserEventCouponListResponseDto.builder()
                .coupons(coupons)
                .build();
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }
}
