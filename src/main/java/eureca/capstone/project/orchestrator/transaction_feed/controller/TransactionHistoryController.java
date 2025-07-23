package eureca.capstone.project.orchestrator.transaction_feed.controller;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.enums.TransactionHistoryType;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.GetTransactionHistoryResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.service.DataTransactionHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

    @GetMapping
    @Operation(summary = "거래 내역 조회 API", description = "자신의 구매 또는 판매 내역을 조회합니다.")
    public BaseResponseDto<Page<GetTransactionHistoryResponseDto>> getMyTransactionHistory(
            @AuthenticationPrincipal CustomUserDetailsDto userDetails,
            @RequestParam(name = "type", defaultValue = "ALL") TransactionHistoryType type,
            @PageableDefault(size = 10, sort = "transactionDate") Pageable pageable
    ) {
        Page<GetTransactionHistoryResponseDto> history = dataTransactionHistoryService.getTransactionHistory(
                userDetails.getEmail(),
                type,
                pageable
        );
        return BaseResponseDto.success(history);
    }
}
