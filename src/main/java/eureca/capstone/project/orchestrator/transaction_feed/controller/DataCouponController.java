package eureca.capstone.project.orchestrator.transaction_feed.controller;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.UserDataCouponDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.UseDataCouponResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.service.DataCouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "데이터 충전권 API", description = "사용자 데이터 충전권 조회 및 사용 API")
@RestController
@RequestMapping("/orchestrator/data-coupon")
@RequiredArgsConstructor
public class DataCouponController {
    private final DataCouponService dataCouponService;

    @Operation(summary = "데이터 충전권 목록 조회 API", description = """
            ## 사용자가 보유한 데이터 충전권 목록을 페이징하여 조회합니다.
            데이터 판매글을 구매하면, 해당 데이터 양만큼의 충전권이 발급됩니다.
            
            ***
            
            ### 📥 요청 파라미터 (Query Parameters)
            | 이름 | 타입 | 필수 | 설명 | 기타 |
            |---|---|:---:|---|---|
            | `pageable` | `Object`| X | 페이지 정보 (`page`, `size`, `sort`) | 기본값: `size=10`, `sort=createdAt,DESC` |
            
            ### 🔑 권한
            * `ROLE_USER` (사용자 로그인 필요)
            
            ### ❌ 주요 실패 코드
            * `20000` (USER_NOT_FOUND): 유효하지 않은 토큰으로 요청하여 사용자를 찾을 수 없을 경우 발생합니다.
            
            ### 📝 참고 사항
            * 충전권의 `status`는 `ISSUED`(사용 가능), `USED`(사용 완료), `EXPIRED`(기간 만료) 중 하나입니다.
            """)
    @GetMapping
    public BaseResponseDto<Page<UserDataCouponDto>> getUserDataCouponList(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @PageableDefault(size = 10, sort = "createdAt", direction = Direction.DESC) Pageable pageable
    ) {
        Page<UserDataCouponDto> couponPage = dataCouponService.getUserDataCouponList(customUserDetailsDto.getEmail(), pageable);
        return BaseResponseDto.success(couponPage);
    }

    @Operation(summary = "데이터 충전권 사용 API", description = """
            ## 데이터 충전권을 사용하여 사용자의 '구매 데이터'를 충전합니다.
            성공 시, 충전권은 '사용 완료' 상태로 변경되며 재사용이 불가능합니다.
            
            ***
            
            ### 📥 요청 파라미터 (Path Variable)
            | 이름 | 타입 | 필수 | 설명 |
            |---|---|:---:|---|
            | `userDataCouponId` | `Long` | O | 사용할 데이터 충전권의 ID |
            
            ### 🔑 권한
            * `ROLE_USER` (사용자 로그인 필요)
            
            ### ❌ 주요 실패 코드
            * `30020` (DATA_COUPON_NOT_FOUND): 존재하지 않는 충전권 ID일 경우 발생합니다.
            * `30021` (DATA_COUPON_ACCESS_DENIED): 다른 사용자의 충전권을 사용하려고 시도할 경우 발생합니다.
            * `30022` (DATA_COUPON_ALREADY_USED): 이미 사용되었거나 유효하지 않은 상태의 충전권일 경우 발생합니다.
            * `30023` (DATA_COUPON_EXPIRED): 유효 기간(발급 후 24시간)이 만료된 충전권일 경우 발생합니다.
            * `20000` (USER_NOT_FOUND): 유효하지 않은 토큰으로 요청할 경우 발생합니다.
            
            ### 📝 참고 사항
            * 데이터 충전권은 발급된 시점으로부터 **24시간**의 유효 기간을 가집니다.
            * `status`가 `ISSUED`인 충전권만 사용할 수 있습니다.
            """)
    @PostMapping("/{userDataCouponId}/use")
    public BaseResponseDto<UseDataCouponResponseDto> useDataCoupon(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @Parameter(description = "사용할 데이터 충전권의 ID") @PathVariable Long userDataCouponId
    ) {
        UseDataCouponResponseDto response = dataCouponService.useDataCoupon(customUserDetailsDto.getEmail(), userDataCouponId);
        return BaseResponseDto.success(response);
    }
}