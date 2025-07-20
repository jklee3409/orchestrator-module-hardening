package eureca.capstone.project.orchestrator.transaction_feed.controller;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.GetFeedSummaryResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.service.RecommendService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orchestrator/recommend")
@RequiredArgsConstructor
public class RecommendController {
    private final RecommendService recommendService;

    @GetMapping
    @Operation(summary = "추천 상품 조회 API", description = "메인 페이지의 추천 상품을 조회합니다. 추천 알고리즘에 따라 상품이 추천됩니다.")
    public BaseResponseDto<List<GetFeedSummaryResponseDto>> getRecommendedFeeds(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto
    ) {
        List<GetFeedSummaryResponseDto> response = recommendService.recommendFeed(customUserDetailsDto);
        return BaseResponseDto.success(response);
    }

    @GetMapping("/related/{transactionFeedId}")
    @Operation(summary = "연관 상품 조회 API", description = "상품 상세 페이지에서 연관 상품을 조회합니다. 상품 ID를 기반으로 연관 상품이 추천됩니다.")
    public BaseResponseDto<List<GetFeedSummaryResponseDto>> getRelatedFeeds(
            @PathVariable Long transactionFeedId
    ) {
        List<GetFeedSummaryResponseDto> response = recommendService.recommendRelateFeeds(transactionFeedId);
        return BaseResponseDto.success(response);
    }
}
