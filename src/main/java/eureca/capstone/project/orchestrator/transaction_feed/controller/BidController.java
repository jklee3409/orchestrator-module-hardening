package eureca.capstone.project.orchestrator.transaction_feed.controller;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.PlaceBidRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.service.BidService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orchestrator/bid")
@RequiredArgsConstructor
public class BidController {
    private final BidService bidService;

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
