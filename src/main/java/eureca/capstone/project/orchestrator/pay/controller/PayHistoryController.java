package eureca.capstone.project.orchestrator.pay.controller;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.pay.dto.PayHistoryDto.PayHistorySimpleDto;
import eureca.capstone.project.orchestrator.pay.dto.response.PayHistoryDetailResponseDto;
import eureca.capstone.project.orchestrator.pay.service.PayHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "페이 변동 내역 API", description = "페이 충전, 환전, 구매, 판매 등 변동 내역 조회 API")
@RestController
@RequestMapping("/orchestrator/pay-history")
@RequiredArgsConstructor
public class PayHistoryController {
    private final PayHistoryService payHistoryService;

    @Operation(summary = "페이 변동 내역 조회 API", description = """
            ## 사용자의 페이 변동 내역을 페이징하여 조회합니다.
            
            ***
            
            ### 📥 요청 파라미터 (Query Parameters)
            | 이름 | 타입 | 필수 | 설명 | 기타 |
            |---|---|:---:|---|---|
            | `pageable` | `Object`| X | 페이지 정보 (`page`, `size`, `sort`) | 요청이 없을 경우 기본값으로 `size=20`이 적용됩니다. |
            
            ### 🔑 권한
            * `ROLE_USER`(사용자 로그인 필요)
            
            ### ❌ 주요 실패 코드
            * '20000' (USER_NOT_FOUND): 유효하지 않은 토큰으로 요청 시, 해당하는 사용자를 찾을 수 없을 경우 발생합니다.
            
            ### 📝 참고 사항
            * `changeType` (변동 유형)은 "충전", "환전", "구매", "판매" 네 가지입니다.
            * `changedPay` (변동 금액)은 구매, 환전 시에는 음수, 판매 및 충전 시에는 양수 값을 가집니다.
            * 페이 변동 내역은 최신 순으로 정렬되어 반환됩니다.
            """)
    @GetMapping
    public BaseResponseDto<Page<PayHistorySimpleDto>> getPayHistoryList(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<PayHistorySimpleDto> payHistoryList = payHistoryService.getPayHistoryList(customUserDetailsDto.getEmail(), pageable);
        return BaseResponseDto.success(payHistoryList);
    }

    @Operation(summary = "페이 변동 내역 상세 조회 API", description = """
            ## 특정 페이 변동 내역을 상세 조회합니다.
            변동 유형(changeType)에 따라 응답의 `chargeDetail`, `exchangeDetail`, `transactionDetail` 중 하나의 필드에만 데이터가 채워집니다.
            
            ***
            
            ### 📥 요청 파라미터 (Path Variable)
            | 이름 | 타입 | 필수 | 설명 |
            |---|---|:---:|---|
            | `payHistoryId` | `Long` | O | 조회할 페이 변동 내역의 ID |
            
            ### 🔑 권한
            * `ROLE_USER`(사용자 로그인 필요)
            
            ### ❌ 주요 실패 코드
            * '20000' (USER_NOT_FOUND): 유효하지 않은 토큰으로 요청 시, 해당하는 사용자를 찾을 수 없을 경우 발생합니다.
            * '30024' (TRANSACTION_HISTORY_NOT_FOUND): 연관된 거래 내역을 찾을 수 없을 경우 발생합니다.
            * '40000' (PAY_HISTORY_NOT_FOUND): 페이 변동 내역을 찾지 못한 경우 발생합니다.
            * '40001' (CHARGE_HISTORY_NOT_FOUND): 충전 내역을 찾지 못한 경우 발생합니다.
            * '40002' (EXCHANGE_HISTORY_NOT_FOUND): 환전 내역을 찾지 못한 경우 발생합니다.
            * '40055' (CHANGE_TYPE_NOT_FOUND): 페이 변동 유형을 찾지 못한 경우 발생합니다.
            
            ### 📝 참고 사항
            * **변동 유형(changeType)에 따른 응답 상세 정보:**
                * `충전`: `chargeDetail` 필드에 충전 상세 정보가 포함됩니다.
                * `환전`: `exchangeDetail` 필드에 환전 상세 정보가 포함됩니다.
                * `구매` 또는 `판매`: `transactionDetail` 필드에 데이터 거래 상세 정보가 포함됩니다.
            * 각 `~Detail` 객체가 null이 아닐 경우 해당 유형의 상세 내역입니다.
            """)
    @GetMapping("/{payHistoryId}")
    public BaseResponseDto<PayHistoryDetailResponseDto> getPayHistoryDetail(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @Parameter(description = "조회할 페이 변동 내역의 ID") @PathVariable Long payHistoryId
    ) {
        PayHistoryDetailResponseDto payHistoryDetail = payHistoryService.getPayHistoryDetail(customUserDetailsDto.getEmail(), payHistoryId);
        return BaseResponseDto.success(payHistoryDetail);
    }
}