package eureca.capstone.project.orchestrator.pay.service;

import eureca.capstone.project.orchestrator.pay.dto.response.GetUserEventCouponListResponseDto;

public interface UserEventCouponService {
    GetUserEventCouponListResponseDto getAvailableCoupons(String email);
}
