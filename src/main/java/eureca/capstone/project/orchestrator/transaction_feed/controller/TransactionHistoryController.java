package eureca.capstone.project.orchestrator.transaction_feed.controller;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.enums.TransactionHistoryType;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.GetTransactionHistoryResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.service.DataTransactionHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orchestrator/transaction-history")
@RequiredArgsConstructor
public class TransactionHistoryController {
    private final DataTransactionHistoryService dataTransactionHistoryService;

    @Operation(summary = "데이터 거래 내역 조회 API", description = """
            ## 자신의 데이터 구매 또는 판매 내역을 페이징하여 조회합니다.
            `type` 파라미터를 사용하여 전체, 구매, 또는 판매 내역을 필터링할 수 있습니다.
            
            ***
            
            ### 📥 요청 파라미터 (Query Parameters)
            | 이름 | 타입 | 필수 | 설명 | 기타 |
            |---|---|:---:|---|---|
            | `type` | `String` | X | 조회할 거래 유형 (`ALL`, `PURCHASE`, `SALE`) | 기본값: `ALL` |
            | `pageable` | `Object`| X | 페이지 정보 (`page`, `size`, `sort`) | 기본값: `size=10`, `sort=createdAt,DESC` |
            
            ### 🔑 권한
            * `ROLE_USER` (사용자 로그인 필요)
            
            ### ❌ 주요 실패 코드
            * `20000` (USER_NOT_FOUND): 유효하지 않은 토큰으로 요청하여 사용자를 찾을 수 없을 경우 발생합니다.
            
            ### 📝 참고 사항
            * 응답의 `transactionType` 필드는 **API를 요청한 사용자 기준**에서 "구매" 또는 "판매"를 나타냅니다.
            * `otherPartyNickname` 필드는 거래 상대방의 닉네임을 의미합니다.
            * 거래 시간을 기준으로 내림차순 정렬되어 반환됩니다.
            """)
    @GetMapping
    public BaseResponseDto<Page<GetTransactionHistoryResponseDto>> getMyTransactionHistory(
            @AuthenticationPrincipal CustomUserDetailsDto userDetails,
            @RequestParam(name = "type", defaultValue = "ALL") TransactionHistoryType type,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<GetTransactionHistoryResponseDto> history = dataTransactionHistoryService.getTransactionHistory(
                userDetails.getEmail(),
                type,
                pageable
        );
        return BaseResponseDto.success(history);
    }
}