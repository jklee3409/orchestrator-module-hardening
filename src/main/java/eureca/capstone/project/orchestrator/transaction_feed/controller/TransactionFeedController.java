package eureca.capstone.project.orchestrator.transaction_feed.controller;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.CreateFeedRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.FeedSearchRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.UpdateFeedRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.CreateFeedResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.GetFeedDetailResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.GetFeedSummaryResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.UpdateFeedResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.service.TransactionFeedService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/orchestrator/transaction-feed")
@RestController
@RequiredArgsConstructor
public class TransactionFeedController {

    private final TransactionFeedService transactionFeedService;

    @PostMapping("/reindex-for-debug")
    public BaseResponseDto<Void> reindexFeeds() {
        transactionFeedService.reindexAllFeeds();
        return BaseResponseDto.voidSuccess();
    }

    // TODO: 찜 여부 조회 필요
    @GetMapping("/search")
    @Operation(summary = "판매글 목록 조회 및 검색 API", description = "다양한 필터와 정렬 조건으로 판매글을 조회/검색합니다.<br>"
            + "status: [ON_SALE], [EXPIRED], [COMPLETED]<br>"
            + "sortBy: [LATEST], [PRICE_HIGH], [PRICE_LOW] -> pageable 의 sorting 은 무시하시면 됩니다.")
    public BaseResponseDto<Page<GetFeedSummaryResponseDto>> searchFeeds(
            @ModelAttribute FeedSearchRequestDto requestDto,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<GetFeedSummaryResponseDto> response = transactionFeedService.searchFeeds(requestDto, pageable);
        return BaseResponseDto.success(response);
    }

    // TODO: 찜 여부, 찜 횟수, 입찰 판매라면 현재 최고가 조회 필요
    @GetMapping("/{transactionFeedId}")
    @Operation(summary = "판매글 상세 조회 API [아직 개발중]", description = "판매글의 상세 정보를 조회합니다. 찜 여부, 찜 횟수, 입찰 판매 시 현재 최고가 조회 기능은 아직 연동되지 않았습니다.")
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
