package eureca.capstone.project.orchestrator.transaction_feed.controller;


import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.CreateFeedRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.UpdateFeedRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.CreateFeedResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.GetFeedDetailResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.UpdateFeedResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.service.TransactionFeedService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/orchestrator/transaction-feed")
@RestController
@RequiredArgsConstructor
public class TransactionFeedController {

    private final TransactionFeedService transactionFeedService;

    @GetMapping("/{transactionFeedId}")
    @Operation(summary = "판매글 상세 조회 API [아직 개발중]", description = "판매글의 상세 정보를 조회합니다.")
    public BaseResponseDto<GetFeedDetailResponseDto> getFeedDetail(@PathVariable Long transactionFeedId) {
        GetFeedDetailResponseDto getFeedDetailResponseDto = transactionFeedService.getFeedDetail(transactionFeedId);
        return BaseResponseDto.success(getFeedDetailResponseDto);
    }


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

    @DeleteMapping("/{transactionFeedId}")
    @Operation(summary = "판매글 삭제 API", description = "로그인한 사용자가 자신의 판매글을 삭제합니다.")
    public BaseResponseDto<Void> deleteFeed(
            @PathVariable Long transactionFeedId,
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto
    ) {
        transactionFeedService.deleteFeed(customUserDetailsDto.getEmail(), transactionFeedId);
        return BaseResponseDto.voidSuccess();
    }
}
