package eureca.capstone.project.orchestrator.pay.controller;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.pay.dto.response.GetUserEventCouponListResponseDto;
import eureca.capstone.project.orchestrator.pay.dto.response.IssuedCouponResponseDto;
import eureca.capstone.project.orchestrator.pay.service.UserEventCouponService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orchestrator/user-event-coupon")
@RequiredArgsConstructor
public class UserEventCouponController {
    private final UserEventCouponService userEventCouponService;

    @GetMapping("/available")
    @Operation(summary = "사용자 이벤트 쿠폰 목록 조회 API", description = "로그인한 사용자가 페이 충전 시 사용할 수 있는 이벤트 쿠폰 목록을 반환합니다. (페이 충전 화면에서 호출)")
    public BaseResponseDto<GetUserEventCouponListResponseDto> getAvailableCoupons(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto
    ) {
        GetUserEventCouponListResponseDto responseDto = userEventCouponService.getAvailableCoupons(customUserDetailsDto.getEmail());
        return BaseResponseDto.success(responseDto);
    }

    @GetMapping("/{couponId}/issue")
    @Operation(summary = "이벤트 쿠폰 발급 API", description = "로그인한 사용자가 이벤트 쿠폰 발급 클릭 시 사용자에게 이벤트 쿠폰을 발급합니다.")
    public BaseResponseDto<IssuedCouponResponseDto> issueEventCoupons(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @PathVariable("couponId") Long couponId
            ) {
        IssuedCouponResponseDto responseDto = userEventCouponService.issueEventCoupon(couponId, customUserDetailsDto.getEmail());
        return BaseResponseDto.success(responseDto);
    }
}
