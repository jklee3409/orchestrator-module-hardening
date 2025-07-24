package eureca.capstone.project.orchestrator.pay.controller;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.pay.dto.response.GetUserEventCouponListResponseDto;
import eureca.capstone.project.orchestrator.pay.dto.response.IssuedCouponResponseDto;
import eureca.capstone.project.orchestrator.pay.service.UserEventCouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "사용자 이벤트 쿠폰 API", description = "사용자 이벤트 쿠폰 조회, 발급 등 API")
@RestController
@RequestMapping("/orchestrator/user-event-coupon")
@RequiredArgsConstructor
public class UserEventCouponController {
    private final UserEventCouponService userEventCouponService;

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
    @GetMapping("/available")
    public BaseResponseDto<GetUserEventCouponListResponseDto> getAvailableCoupons(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto
    ) {
        GetUserEventCouponListResponseDto responseDto = userEventCouponService.getAvailableCoupons(customUserDetailsDto.getEmail());
        return BaseResponseDto.success(responseDto);
    }

    @Operation(summary = "이벤트 쿠폰 발급 API", description = """
            ## 사용자에게 특정 이벤트 쿠폰을 발급합니다.
            사용자가 이벤트 페이지 등에서 쿠폰 발급 버튼을 클릭했을 때 호출됩니다.
            
            ***
            
            ### 📥 요청 파라미터 (Path Variable)
            | 이름 | 타입 | 필수 | 설명 |
            |---|---|:---:|---|
            | `couponId` | `Long` | O | 발급받을 이벤트 쿠폰의 ID |
            
            ### 🔑 권한
            * `ROLE_USER` (사용자 로그인 필요)
            
            ### ❌ 주요 실패 코드
            * `20000` (USER_NOT_FOUND): 유효하지 않은 토큰으로 요청 시, 해당하는 사용자를 찾을 수 없을 경우 발생합니다.
            * `40023` (EVENT_COUPON_NOT_FOUND): 존재하지 않는 `couponId`를 요청했을 경우 발생합니다.
            * `40024` (USER_EVENT_COUPON_ALREADY_EXISTS): 사용자가 이미 해당 이벤트 쿠폰을 발급받은 경우 발생합니다.
            
            ### 📝 참고 사항
            * 쿠폰은 발급 시점으로부터 **1개월**의 유효 기간을 가집니다.
            * 한 번 발급받은 이벤트 쿠폰은 중복해서 발급받을 수 없습니다.
            """)
    @PostMapping("/{couponId}/issue")
    public BaseResponseDto<IssuedCouponResponseDto> issueEventCoupons(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @Parameter(description = "발급받을 이벤트 쿠폰의 ID") @PathVariable("couponId") Long couponId
    ) {
        IssuedCouponResponseDto responseDto = userEventCouponService.issueEventCoupon(couponId, customUserDetailsDto.getEmail());
        return BaseResponseDto.success(responseDto);
    }
}