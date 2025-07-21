package eureca.capstone.project.orchestrator.transaction_feed.controller;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.PlaceBidRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.GetBidHistoryResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.service.BidService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orchestrator/bid")
@RequiredArgsConstructor
public class BidController {
    private final BidService bidService;

    @GetMapping("/{transactionFeedId}")
    @Operation(summary = "입찰 내역 조회 API", description = "판매글의 입찰 내역을 조회합니다.")
    public BaseResponseDto<GetBidHistoryResponseDto> getBidHistory(
            @PathVariable Long transactionFeedId
    ) {
        GetBidHistoryResponseDto response = bidService.getBidHistory(transactionFeedId);
        return BaseResponseDto.success(response);
    }

    @PostMapping
    @Operation(summary = "입찰 API", description = "사용자가 판매글에 입찰합니다.")
    public BaseResponseDto<Void> placeBid(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @RequestBody PlaceBidRequestDto placeBidRequestDto
    ) {
        bidService.placeBid(customUserDetailsDto.getEmail(), placeBidRequestDto);
        return BaseResponseDto.voidSuccess();
    }
}
