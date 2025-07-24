package eureca.capstone.project.orchestrator.transaction_feed.controller;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.enums.SalesTypeFilter;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.AddWishFeedRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.RemoveWishFeedsRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.GetFeedSummaryResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.service.LikedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

@Tag(name = "찜 API", description = "판매글 찜 목록 조회, 등록, 삭제 API")
@RestController
@RequestMapping("/orchestrator/wish")
@RequiredArgsConstructor
public class LikedController {
    private final LikedService likedService;

    @GetMapping
    @Operation(summary = "찜 목록 조회", description = """
            ## 로그인한 사용자의 찜 목록을 판매 유형에 따라 필터링하여 조회합니다.
            
            ***
            
            ### 📥 요청 파라미터 (Query Parameters)
            * `filter`: 판매 유형 필터(default: `ALL`) (문자열, `ALL`, `NORMAL`, `BID`)
            * `pageable`: 페이징 정보 (기본값: `size=20, sort=createdAt,desc`)
            
            ### 🔑 권한
            * `ROLE_USER` (사용자 로그인 필요)
            
            ### ❌ 주요 실패 코드
            * `20000` (USER_NOT_FOUND): 사용자를 찾을 수 없는 경우
            
            ### 📝 참고 사항
            * 'sort' 파라미터는 지우고 테스트 부탁드립니다.
            """)
    public BaseResponseDto<Page<GetFeedSummaryResponseDto>> getWishList(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @RequestParam(defaultValue = "ALL") SalesTypeFilter filter,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<GetFeedSummaryResponseDto> response = likedService.getWishList(customUserDetailsDto.getEmail(), filter, pageable);
        return BaseResponseDto.success(response);
    }

    @PostMapping("/wish")
    @Operation(summary = "판매글 찜 등록", description = """
            ## 로그인한 사용자가 자신의 찜 목록에 판매글을 추가합니다.
            
            ***
            
            ### 📥 요청 바디 (Request Body)
            ```json
            {
              "transactionFeedId": 123
            }
            ```
            
            ### 📥 요청 바디 필드 설명
            * `transactionFeedId`: 찜 목록에 추가할 판매글의 ID (숫자)
            
            ### 🔑 권한
            * `ROLE_USER` (사용자 로그인 필요)
            
            ### ❌ 주요 실패 코드
            * `20000` (USER_NOT_FOUND): 사용자를 찾을 수 없는 경우
            * `30003` (TRANSACTION_FEED_NOT_FOUND): 찜할 판매글이 존재하지 않는 경우
            * `30007` (ALREADY_EXISTS_LIKED_LIST): 이미 찜 목록에 추가된 판매글인 경우
            """)
    public BaseResponseDto<Void> addWishFeed(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @RequestBody AddWishFeedRequestDto addWishFeedRequestDto
    ) {
        likedService.addWishFeed(customUserDetailsDto.getEmail(), addWishFeedRequestDto);
        return BaseResponseDto.voidSuccess();
    }

    @DeleteMapping("/wish")
    @Operation(summary = "판매글 찜 삭제", description = """
            ## 로그인한 사용자가 자신의 찜 목록에서 하나 이상의 판매글을 삭제합니다.
            
            ***
            
            ### 📥 요청 바디 (Request Body)
            ```json
            {
              "transactionFeedIds": [123, 124]
            }
            ```
            
            ### 📥 요청 바디 필드 설명
            * `transactionFeedIds`: 찜 목록에서 삭제할 판매글 ID 목록 (숫자 배열)
            
            ### 🔑 권한
            * `ROLE_USER` (사용자 로그인 필요)
            
            ### ❌ 주요 실패 코드
            * `20000` (USER_NOT_FOUND): 사용자를 찾을 수 없는 경우
            """)
    public BaseResponseDto<Void> removeWishFeed(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @RequestBody RemoveWishFeedsRequestDto requestDto
    ) {
        likedService.removeWishFeed(customUserDetailsDto.getEmail(), requestDto);
        return BaseResponseDto.voidSuccess();
    }
}
