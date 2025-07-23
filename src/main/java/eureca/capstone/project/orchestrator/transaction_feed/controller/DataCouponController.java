package eureca.capstone.project.orchestrator.transaction_feed.controller;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.UserDataCouponDto;
import eureca.capstone.project.orchestrator.transaction_feed.service.DataCouponService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
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
    public BaseResponseDto<Page<UserDataCouponDto>> getUserDataCouponList(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @PageableDefault(size = 10, sort = "createdAt", direction = Direction.DESC) Pageable pageable
    ) {
        Page<UserDataCouponDto> couponPage = dataCouponService.getUserDataCouponList(customUserDetailsDto.getEmail(), pageable);
        return BaseResponseDto.success(couponPage);
    }
}
