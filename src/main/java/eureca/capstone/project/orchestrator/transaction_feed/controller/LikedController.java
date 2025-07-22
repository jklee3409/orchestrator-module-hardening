package eureca.capstone.project.orchestrator.transaction_feed.controller;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.enums.WishListFilter;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.AddWishFeedRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.RemoveWishFeedsRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.GetFeedSummaryResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.service.LikedService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orchestrator/wish")
@RequiredArgsConstructor
public class LikedController {
    private final LikedService likedService;

    @GetMapping
    @Operation(summary = "찜 목록 조회 API", description = "로그인한 사용자가 자신의 찜 목록을 조회합니다.<br>"
            + "filter: [ALL], [NORMAL], [BID]")
    public BaseResponseDto<Page<GetFeedSummaryResponseDto>> getWishList(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @RequestParam(defaultValue = "ALL") WishListFilter filter,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<GetFeedSummaryResponseDto> response = likedService.getWishList(customUserDetailsDto.getEmail(), filter, pageable);
        return BaseResponseDto.success(response);
    }

    @PostMapping("/wish")
    @Operation(summary = "판매글 찜 등록 API", description = "로그인한 사용자가 자신의 찜 목록에 판매글을 추가합니다.")
    public BaseResponseDto<Void> addWishFeed(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @RequestBody AddWishFeedRequestDto addWishFeedRequestDto
    ) {
        likedService.addWishFeed(customUserDetailsDto.getEmail(), addWishFeedRequestDto);
        return BaseResponseDto.voidSuccess();
    }

    @DeleteMapping("/wish")
    @Operation(summary = "판매글 찜 삭제 API", description = "로그인한 사용자가 자신의 찜 목록에서 판매글을 삭제합니다.")
    public BaseResponseDto<Void> removeWishFeed(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @RequestBody RemoveWishFeedsRequestDto requestDto
    ) {
        likedService.removeWishFeed(customUserDetailsDto.getEmail(), requestDto);
        return BaseResponseDto.voidSuccess();
    }
}
