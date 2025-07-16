package eureca.capstone.project.orchestrator.transaction_feed.controller;


import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.CreateFeedRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.UpdateFeedRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.CreateFeedResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.UpdateFeedResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.service.TransactionFeedService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/transaction-feed")
@RestController
@RequiredArgsConstructor
public class TransactionFeedController {

    private final TransactionFeedService transactionFeedService;

    @PostMapping
    @Operation(summary = "판매글 등록 API", description = "로그인한 사용자가 판매자가 되어 판매글을 등록합니다.")
    public BaseResponseDto<CreateFeedResponseDto> createFeed(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @RequestBody CreateFeedRequestDto createFeedRequestDto
    ) {
        CreateFeedResponseDto createFeedResponse = transactionFeedService.createFeed(customUserDetailsDto.getEmail(), createFeedRequestDto);
        return BaseResponseDto.success(createFeedResponse);
    }

    @PutMapping
    @Operation(summary = "판매글 수정 API", description = "로그인한 사용자가 자신의 판매글을 수정합니다.")
    public BaseResponseDto<UpdateFeedResponseDto> updateFeed(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @RequestBody UpdateFeedRequestDto updateFeedRequestDto
    ) {
        UpdateFeedResponseDto updateFeedResponse = transactionFeedService.updateFeed(customUserDetailsDto.getEmail(), updateFeedRequestDto);
        return BaseResponseDto.success(updateFeedResponse);
    }
}
