package eureca.capstone.project.orchestrator.transaction_feed.controller;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.PlaceBidRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.GetBidHistoryResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.service.BidService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "입찰 판매글 API", description = "입찰 참여 및 입찰 내역 조회 API")
@RestController
@RequestMapping("/orchestrator/bid")
@RequiredArgsConstructor
public class BidController {
    private final BidService bidService;

    @Operation(summary = "입찰 내역 조회 API", description = """
            ## 특정 입찰 판매글의 전체 입찰 내역을 조회합니다.
            
            ***
            
            ### 📥 요청 파라미터 (Path Variable)
            | 이름 | 타입 | 필수 | 설명 |
            |---|---|:---:|---|
            | `transactionFeedId` | `Long` | O | 조회할 입찰 판매글의 ID |
            
            ### 🔑 권한
            * 모든 사용자
            
            ### ❌ 주요 실패 코드
            * `30003` (TRANSACTION_FEED_NOT_FOUND): 존재하지 않는 판매글 ID일 경우 발생합니다.
            * `30014` (FEED_NOT_AUCTION): 해당 판매글이 입찰 판매 유형이 아닐 경우 발생합니다.
            
            ### 📝 참고 사항
            * 입찰 내역은 최근 입찰 순으로 정렬되어 반환됩니다.
            * 입찰 내역에는 입찰 금액, 입찰자 닉네임, 입찰 시간이 포함됩니다.
            """)
    @GetMapping("/{transactionFeedId}")
    public BaseResponseDto<GetBidHistoryResponseDto> getBidHistory(
            @Parameter(description = "조회할 입찰 판매글의 ID") @PathVariable Long transactionFeedId
    ) {
        GetBidHistoryResponseDto response = bidService.getBidHistory(transactionFeedId);
        return BaseResponseDto.success(response);
    }

    @PreAuthorize("hasAuthority('TRANSACTION')")
    @Operation(summary = "입찰 참여 API", description = """
            ## 입찰 판매글에 입찰을 시도합니다.
            성공 시, 입찰 금액만큼 페이가 차감되며 이전 최고 입찰자에게는 페이가 환불됩니다. 또한 입찰 참여자들에게 실시간 알림이 전송됩니다.
            
            ***
            
            ### 📥 요청 바디 (Request Body)
            ```json
            {
              "transactionFeedId": 1,
              "bidAmount": 15000
            }
            ```
            
            ### 🔑 권한
            * `ROLE_USER` (사용자 로그인 필요)
            
            ### ❌ 주요 실패 코드
            #### 사전 조건 실패
            * `30009` (SELLER_CANNOT_BID): 판매자가 자신의 판매글에 입찰하는 경우          
            * `30010` (AUCTION_NOT_ON_SALE): 판매글이 판매중 상태가 아닌 경우
            * `30014` (FEED_NOT_AUCTION): 해당 판매글이 입찰 판매 유형이 아닌 경우
            * `30011` (AUCTION_EXPIRED): 입찰 시간이 만료된 경우
            * `30012` (BID_AMOUNT_TOO_LOW): 입찰 금액이 현재 최고가보다 낮은 경우
            * `30013` (CANNOT_BID_ON_OWN_HIGHEST): 이미 최고 입찰자인 사용자가 다시 입찰하는 경우
            * '30025' (BID_AMOUNT_100_DIVISIBLE): 입찰 금액이 100원 단위로 나누어 떨어지지 않는 경우
            * `60001` (INVALID_TELECOM_COMPANY): 입찰자의 통신사와 판매글의 통신사가 다른 경우
            #### 처리 중 실패
            * `30015` (LUA_SCRIPT_ERROR): Redis 스크립트 실행 중 오류가 발생한 경우
            * `30016` (BID_PROCESSING_FAILED): 입찰 성공 후 DB 처리(페이 차감/환불 등)에 실패하여 롤백된 경우
            * `40057` (USER_PAY_LACK): 입찰자의 페이가 부족한 경우 (DB 처리 중 확인)
            
            ### 📝 참고 사항
            * 입찰 금액(`bidAmount`)은 **100원 단위**여야 합니다.
            """)
    @PostMapping
    public BaseResponseDto<Void> placeBid(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @RequestBody PlaceBidRequestDto placeBidRequestDto
    ) {
        bidService.placeBid(customUserDetailsDto.getEmail(), placeBidRequestDto);
        return BaseResponseDto.voidSuccess();
    }
}