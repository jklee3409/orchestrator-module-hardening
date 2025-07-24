package eureca.capstone.project.orchestrator.pay.service.impl;

import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.common.exception.code.ErrorCode;
import eureca.capstone.project.orchestrator.common.exception.custom.EventCouponNotFoundException;
import eureca.capstone.project.orchestrator.common.exception.custom.InternalServerException;
import eureca.capstone.project.orchestrator.common.exception.custom.UserEventCouponAlreadyExistsException;
import eureca.capstone.project.orchestrator.common.exception.custom.UserNotFoundException;
import eureca.capstone.project.orchestrator.common.util.StatusManager;
import eureca.capstone.project.orchestrator.pay.dto.UserEventCouponDto;
import eureca.capstone.project.orchestrator.pay.dto.response.GetUserEventCouponListResponseDto;
import eureca.capstone.project.orchestrator.pay.dto.response.IssuedCouponResponseDto;
import eureca.capstone.project.orchestrator.pay.entity.EventCoupon;
import eureca.capstone.project.orchestrator.pay.entity.UserEventCoupon;
import eureca.capstone.project.orchestrator.pay.repository.EventCouponRepository;
import eureca.capstone.project.orchestrator.pay.repository.UserEventCouponRepository;
import eureca.capstone.project.orchestrator.pay.repository.custom.UserEventCouponRepositoryCustom;
import eureca.capstone.project.orchestrator.pay.service.UserEventCouponService;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventCouponServiceImpl implements UserEventCouponService {
    private final UserEventCouponRepositoryCustom userEventCouponRepositoryCustom;
    private final UserEventCouponRepository userEventCouponRepository;
    private final EventCouponRepository eventCouponRepository;
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

    @Override
    public UserEventCoupon validateAndGetCoupon(Long userEventCouponId, User user) {
        log.info("[validateAndGetCoupon] 사용자: {} 이벤트 쿠폰 검증 시작", user.getEmail());

        UserEventCoupon coupon = userEventCouponRepositoryCustom.findCouponDetailsById(userEventCouponId)
                .orElseThrow(() -> new InternalServerException(ErrorCode.USER_EVENT_COUPON_NOT_FOUND));
        log.info("[validateAndGetCoupon] 이벤트 쿠폰 조회 완료");

        if (!coupon.getUser().equals(user)) throw new InternalServerException(ErrorCode.USER_EVENT_COUPON_NOT_MATCHED);
        log.info("[validateAndGetCoupon] 사용자와 이벤트 쿠폰 소유자 일치");

        Status issuedStatus = statusManager.getStatus("COUPON", "ISSUED");
        if (!issuedStatus.equals(coupon.getStatus()) || coupon.getExpiresAt().isBefore(LocalDateTime.now())) throw new InternalServerException(ErrorCode.USER_EVENT_COUPON_EXPIRED);
        log.info("[validateAndGetCoupon] 이벤트 쿠폰 검증 완료");

        return coupon;
    }

    @Override
    @Transactional
    public void useCoupon(UserEventCoupon coupon) {
        log.info("[useCoupon] 사용자 이벤트 쿠폰 사용 처리 시작. 쿠폰 ID: {}", coupon.getUserEventCouponId());

        Status usedStatus = statusManager.getStatus("COUPON", "USED");
        coupon.changeStatus(usedStatus);
        log.info("[useCoupon] 사용자 이벤트 쿠폰 사용 처리 완료. 쿠폰 ID: {}", coupon.getUserEventCouponId());
    }

    @Override
    public void revertCoupon(UserEventCoupon coupon) {
        log.info("[revertCoupon] 사용자 이벤트 쿠폰 상태 되돌리기 시작. 쿠폰 ID: {}", coupon.getUserEventCouponId());

        Status issuedStatus = statusManager.getStatus("COUPON", "ISSUED");
        coupon.changeStatus(issuedStatus);
        log.info("[revertCoupon] 사용자 이벤트 쿠폰 상태 ISSUED로 변경 완료. 쿠폰 ID: {}", coupon.getUserEventCouponId());
    }

    @Override
    public IssuedCouponResponseDto issueEventCoupon(Long couponId, String email) {
        log.info("[issueEventCoupon] 이벤트 쿠폰 발급 시작. 쿠폰 ID: {}", couponId);
        User user = findUserByEmail(email);

        EventCoupon eventCoupon = eventCouponRepository.findById(couponId)
                .orElseThrow(EventCouponNotFoundException::new);

        if(userEventCouponRepository.existsByUserAndEventCoupon(user, eventCoupon)){
            throw new UserEventCouponAlreadyExistsException();
        }

        Status issuedStatus = statusManager.getStatus("COUPON", "ISSUED");

        LocalDateTime expiresAt = LocalDateTime.now().plusMonths(1);

        log.info("[issueEventCoupon] 이벤트 쿠폰 만료 일자: {}", expiresAt);
        UserEventCoupon userEventCoupon = UserEventCoupon.builder()
                .user(user)
                .eventCoupon(eventCoupon)
                .status(issuedStatus)
                .expiresAt(expiresAt)
                .build();
        userEventCouponRepository.save(userEventCoupon);

        log.info("[issueEventCoupon] 이벤트 쿠폰 발급 완료. id: {}", userEventCoupon.getUserEventCouponId());
        return IssuedCouponResponseDto.builder().id(userEventCoupon.getUserEventCouponId()).build();
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }
}
