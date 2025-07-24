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
    @Operation(summary = "사용 가능한 이벤트 쿠폰 목록 조회 API", description = """
            ## 로그인한 사용자가 페이 충전 시 사용할 수 있는 이벤트 쿠폰 목록을 조회합니다.
            페이 충전 화면에서 사용자가 보유한 쿠폰을 보여줄 때 혹은 마이페이지에서 호출됩니다.
            
            ***
            
            ### 📥 요청 파라미터
            * 별도의 요청 파라미터는 없으며, **Authorization 헤더의 토큰**으로 사용자를 식별합니다.
            
            ### 🔑 권한
            * `ROLE_USER` (사용자 로그인 필요)
            
            ### ❌ 주요 실패 코드
            * `20000` (USER_NOT_FOUND): 유효하지 않은 토큰으로 요청 시, 해당하는 사용자를 찾을 수 없을 경우 발생합니다.
            
            ### 📝 참고 사항
            * 사용 가능한 쿠폰이 없을 경우, `coupons` 필드는 빈 리스트(`[]`)로 반환됩니다.
            * 만료되었거나 이미 사용한 쿠폰은 목록에 포함되지 않습니다.
            """)
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
