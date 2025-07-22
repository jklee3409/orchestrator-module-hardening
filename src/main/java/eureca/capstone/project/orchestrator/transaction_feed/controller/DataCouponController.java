package eureca.capstone.project.orchestrator.transaction_feed.controller;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.GetUserDataCouponListResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.service.DataCouponService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orchestrator/data-coupon")
@RequiredArgsConstructor
public class DataCouponController {
    private final DataCouponService dataCouponService;

    @GetMapping
    @Operation(summary = "데이터 충전권 목록 조회 API", description = "로그인한 사용자가 자신이 소유한 데이터 충전권 목록을 조회합니다.")
    public BaseResponseDto<GetUserDataCouponListResponseDto> getUserDataCouponList(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto
    ) {
        GetUserDataCouponListResponseDto responseDto = dataCouponService.getUserDataCouponList(customUserDetailsDto.getEmail());
        return BaseResponseDto.success(responseDto);
    }
}
