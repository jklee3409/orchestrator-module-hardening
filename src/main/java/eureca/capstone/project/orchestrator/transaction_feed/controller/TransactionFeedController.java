package eureca.capstone.project.orchestrator.transaction_feed.controller;


import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.CreateFeedRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.CreateFeedResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.service.TransactionFeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/transaction-feed")
@RestController
@RequiredArgsConstructor
public class TransactionFeedController {

    private final TransactionFeedService transactionFeedService;

    @PostMapping
    public BaseResponseDto<CreateFeedResponseDto> createFeed(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @RequestBody CreateFeedRequestDto createFeedRequestDto
    ) {
        CreateFeedResponseDto createFeedResponse = transactionFeedService.createFeed(customUserDetailsDto.getEmail(), createFeedRequestDto);
        return BaseResponseDto.success(createFeedResponse);
    }
}
