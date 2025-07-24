package eureca.capstone.project.orchestrator.pay.service;

import eureca.capstone.project.orchestrator.pay.dto.response.GetUserEventCouponListResponseDto;
import eureca.capstone.project.orchestrator.pay.dto.response.IssuedCouponResponseDto;
import eureca.capstone.project.orchestrator.pay.entity.UserEventCoupon;
import eureca.capstone.project.orchestrator.user.entity.User;

public interface UserEventCouponService {
    GetUserEventCouponListResponseDto getAvailableCoupons(String email);
    UserEventCoupon validateAndGetCoupon(Long userEventCouponId, User user);
    void useCoupon(UserEventCoupon userEventCoupon);
    void revertCoupon(UserEventCoupon userEventCoupon);
    IssuedCouponResponseDto issueEventCoupon(Long couponId, String email);
}
