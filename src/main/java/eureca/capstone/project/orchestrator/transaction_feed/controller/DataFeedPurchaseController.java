package eureca.capstone.project.orchestrator.transaction_feed.controller;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.PurchaseRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.PurchaseResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.service.DataFeedPurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "데이터 판매글 구매 API", description = "일반 판매글 구매 API")
@RestController
@RequestMapping("/orchestrator/data-feed/purchase")
@RequiredArgsConstructor
public class DataFeedPurchaseController {
    private final DataFeedPurchaseService dataFeedPurchaseService;

    @Operation(summary = "일반 판매글 즉시 구매 API", description = """
            ## '일반 판매' 유형의 데이터 판매글을 구매합니다.
            구매 성공 시, 판매글 가격만큼 구매자의 페이가 차감되고 판매자에게 지급됩니다. 구매자에게는 해당 데이터 양만큼의 **'데이터 충전권'**이 발급됩니다.
            
            ***
            
            ### 📥 요청 바디 (Request Body)
            ```json
            {
              "transactionFeedId": 2
            }
            ```
            
            ### 🔑 권한
            * `ROLE_USER` (사용자 로그인 필요)
            
            ### ❌ 주요 실패 코드
            * `30003` (TRANSACTION_FEED_NOT_FOUND): 존재하지 않거나 삭제된 판매글 ID일 경우
            * `30017` (FEED_NOT_ON_SALE): 판매글이 '판매중' 상태가 아닐 경우
            * `30018` (CANNOT_BUY_OWN_FEED): 자신의 판매글을 구매하려는 경우
            * `30019` (CANNOT_BUY_AUCTION_FEED): 입찰 판매글을 즉시 구매하려는 경우
            * `40057` (USER_PAY_LACK): 구매자의 페이가 부족한 경우
            * `60001` (INVALID_TELECOM_COMPANY): 구매자의 통신사와 판매글의 통신사가 다른 경우
            * `20000` (USER_NOT_FOUND): 유효하지 않은 토큰으로 요청할 경우
            
            ### 📝 참고 사항
            * 이 API는 **입찰 판매글에는 사용할 수 없습니다.**
            * 구매가 완료되면 판매글의 상태는 '거래 완료'로 변경됩니다.
            * 구매 완료 시, 판매자와 구매자에게 각각 알림이 전송됩니다.
            """)
    @PostMapping
    public BaseResponseDto<PurchaseResponseDto> purchase(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @RequestBody PurchaseRequestDto requestDto
    ) {
        PurchaseResponseDto responseDto = dataFeedPurchaseService.purchase(customUserDetailsDto.getEmail(), requestDto.getTransactionFeedId());
        return BaseResponseDto.success(responseDto);
    }
}