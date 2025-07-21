package eureca.capstone.project.orchestrator.transaction_feed.controller;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.PurchaseRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.PurchaseResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.service.DataFeedPurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orchestrator/data-feed/purchase")
@RequiredArgsConstructor
public class DataFeedPurchaseController {
    private final DataFeedPurchaseService dataFeedPurchaseService;

    @PostMapping
    @Operation(summary = "데이터 판매글 구매 API", description = "판매글을 구매합니다.")
    public BaseResponseDto<PurchaseResponseDto> purchase(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @RequestBody PurchaseRequestDto requestDto
    ) {
        PurchaseResponseDto responseDto = dataFeedPurchaseService.purchase(customUserDetailsDto.getEmail(), requestDto.getTransactionFeedId());
        return BaseResponseDto.success(responseDto);
    }
}
