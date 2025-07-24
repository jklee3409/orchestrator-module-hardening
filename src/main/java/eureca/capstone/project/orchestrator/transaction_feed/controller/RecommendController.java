package eureca.capstone.project.orchestrator.transaction_feed.controller;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.GetFeedSummaryResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.service.RecommendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "추천 API", description = "메인 페이지 및 상품 상세 페이지 추천 상품 조회 API")
@RestController
@RequestMapping("/orchestrator/recommend")
@RequiredArgsConstructor
public class RecommendController {
    private final RecommendService recommendService;

    @Operation(summary = "메인 페이지 추천 상품 조회 API", description = """
            ## 메인 페이지에 표시될 추천 상품 목록을 조회합니다. (10개)
            사용자의 상태에 따라 다른 추천 로직이 적용됩니다.
            
            ***
            
            ### ✨ 추천 로직
            * **비로그인 사용자**: 전체 판매글 중 **가격이 저렴한 순**으로 추천합니다.
            * **로그인 사용자 (거래 내역 O)**: 사용자의 **평균 구매 가격 및 데이터양과 유사한 상품**을 우선적으로 추천합니다.
            * **로그인 사용자 (거래 내역 X)**: 사용자의 **통신사에 맞는 상품 중 가격이 저렴한 순**으로 추천합니다.
            
            ### 📥 요청 파라미터
            * 별도의 요청 파라미터는 없으며, **Authorization 헤더의 토큰**으로 로그인/비로그인 상태를 구분합니다.
            
            ### 🔑 권한
            * 모든 사용자 (비회원도 조회 가능)
            
            ### ❌ 주요 실패 코드
            * `20000` (USER_NOT_FOUND): 유효하지 않은 토큰으로 요청 시, 해당하는 사용자를 찾을 수 없을 경우 발생합니다.
            
            ### 📝 참고 사항
            * 추천 로직으로 찾은 상품이 10개 미만일 경우, **부족한 만큼 전체 상품에서 가격이 저렴한 순으로 채워** 반환됩니다.
            """)
    @GetMapping
    public BaseResponseDto<List<GetFeedSummaryResponseDto>> getRecommendedFeeds(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto
    ) {
        List<GetFeedSummaryResponseDto> response = recommendService.recommendFeed(customUserDetailsDto);
        return BaseResponseDto.success(response);
    }

    @Operation(summary = "연관 상품 조회 API", description = """
            ## 특정 상품과 연관된 상품 목록을 조회합니다. (최대 4개)
            상품 상세 페이지 하단에 표시되며, 기준 상품과 유사한 상품을 추천합니다.
            
            ***
            
            ### ✨ 추천 로직
            * 기준 상품과 **동일한 통신사 및 판매 유형**을 가진 상품 중에서, **가격과 데이터양이 가장 유사한 순서**로 추천합니다.
            
            ### 📥 요청 파라미터 (Path Variable)
            | 이름 | 타입 | 필수 | 설명 |
            |---|---|:---:|---|
            | `transactionFeedId` | `Long` | O | 연관 상품을 조회할 기준 상품의 ID |
            
            ### 🔑 권한
            * 모든 사용자 (비회원도 조회 가능)
            
            ### ❌ 주요 실패 코드
            * `30003` (TRANSACTION_FEED_NOT_FOUND): 존재하지 않는 `transactionFeedId`를 요청했을 경우 발생합니다.
            * `20000` (USER_NOT_FOUND): 유효하지 않은 토큰으로 요청 시, 해당하는 사용자를 찾을 수 없을 경우 발생합니다.
            
            ### 📝 참고 사항
            * 관련된 상품이 4개 미만일 경우, **부족한 만큼 전체 상품에서 가격이 저렴한 순으로 채워** 반환됩니다.
            """)
    @GetMapping("/related/{transactionFeedId}")
    public BaseResponseDto<List<GetFeedSummaryResponseDto>> getRelatedFeeds(
            @Parameter(description = "연관 상품을 조회할 기준 상품의 ID") @PathVariable Long transactionFeedId,
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto
    ) {
        List<GetFeedSummaryResponseDto> response = recommendService.recommendRelateFeeds(transactionFeedId, customUserDetailsDto);
        return BaseResponseDto.success(response);
    }
}