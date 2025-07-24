package eureca.capstone.project.orchestrator.transaction_feed.controller;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.enums.SalesTypeFilter;
import eureca.capstone.project.orchestrator.transaction_feed.dto.enums.StatusFilter;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.CreateFeedRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.FeedSearchRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.UpdateFeedRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.CreateFeedResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.GetFeedDetailResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.GetFeedSummaryResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.UpdateFeedResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.service.TransactionFeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "판매글 API", description = "판매글 조회, 검색, 생성, 수정, 삭제 관련 API")
@RequestMapping("/orchestrator/transaction-feed")
@RestController
@RequiredArgsConstructor
public class TransactionFeedController {

    private final TransactionFeedService transactionFeedService;

    @GetMapping("/search")
    @Operation(summary = "판매글 목록 조회 및 검색", description = """
            ## 다양한 필터와 정렬 조건으로 판매글을 조회/검색합니다.
            
            ***
            
            ### 📥 요청 파라미터 (Query Parameters)
            * `keyword`: 검색어 (문자열, 예: "SKT", "데이터")
            * `telecomCompanyIds`: 통신사 ID 목록 (숫자 배열, 예: [1], [1,2,3])
            * `salesTypeIds`: 판매 유형 ID 목록 (숫자 배열, 예: [1], [1,2]])
            * `statuses`: 판매글 상태 목록 (문자열 배열, 예: `ON_SALE`)
            * `minPrice`, `maxPrice`: 최소/최대 가격 범위 (숫자)
            * `minDataAmount`, `maxDataAmount`: 최소/최대 데이터양 범위 (숫자, MB 단위)
            * `sortBy`: 정렬 기준 (문자열, `LATEST`, `PRICE_HIGH`, `PRICE_LOW`)
            
            ### 기타
            * `sortBy`는 필수입니다.
            * `page`,`size` 지정 가능 (쿼리파라미터로 전송하시면 됩니다. 디폴트값[size = 10, page=0])
            * **원하는 필터조건만 쿼리파라미터로 추가하셔서 전송하시면 됩니다.**
            
            ### 🔑 권한
            * 없음 (비로그인 사용자도 가능)
            """)
    public BaseResponseDto<Page<GetFeedSummaryResponseDto>> searchFeeds(
            @ModelAttribute FeedSearchRequestDto requestDto,
            @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto
    ) {
        Page<GetFeedSummaryResponseDto> response = transactionFeedService.searchFeeds(requestDto, pageable, customUserDetailsDto);
        return BaseResponseDto.success(response);
    }

    @GetMapping("/{transactionFeedId}")
    @Operation(summary = "판매글 상세 조회", description = """
            ## 특정 판매글의 상세 정보를 조회합니다.
            
            ***
            
            ### 📥 요청 파라미터 (Path Variable)
            | 이름 | 타입 | 필수 | 설명 |
            |---|---|:---:|---|
            | `transactionFeedId` | `Long` | O | 조회할 판매글의 ID |
            
            ### 🔑 권한
            * 없음 (비로그인 사용자도 가능)
            
            ### ❌ 주요 실패 코드
            * `30003` (TRANSACTION_FEED_NOT_FOUND): 해당 ID의 판매글이 존재하지 않을 경우
            """)
    public BaseResponseDto<GetFeedDetailResponseDto> getFeedDetail(
            @PathVariable Long transactionFeedId,
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto
    ) {
        GetFeedDetailResponseDto getFeedDetailResponseDto = transactionFeedService.getFeedDetail(transactionFeedId, customUserDetailsDto);
        return BaseResponseDto.success(getFeedDetailResponseDto);
    }

    @GetMapping("/my-feeds")
    @Operation(summary = "내 판매글 목록 조회", description = """
            ## 로그인한 사용자가 자신이 등록한 판매글 목록을 조회합니다.
            
            ***
            
            ### 📥 요청 파라미터 (Query Parameters)
            * `filter`: 판매 유형 필터(default: `ALL`) (문자열, `ALL`, `NORMAL`, `BID`)
            * `status`: 판매 상태 필터(default: `ALL`) (문자열, `ALL`, `ON_SALE`, `EXPIRED`, `COMPLETED`)
            * `pageable`: 페이징 정보 (예: `sort=createdAt,desc`)
                * `createdAt,desc`: 최신순 (기본값)
                * `salesPrice,desc`: 가격 높은 순
                * `salesPrice,asc`: 가격 낮은 순
            
            ### 🔑 권한
            * `ROLE_USER` (사용자 로그인 필요)
            
            ### ❌ 주요 실패 코드
            * `20000` (USER_NOT_FOUND): 사용자를 찾을 수 없는 경우
            """)
    public BaseResponseDto<Page<GetFeedSummaryResponseDto>> getMyFeeds(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @RequestParam(required = false, defaultValue = "ALL") SalesTypeFilter filter,
            @RequestParam(required = false, defaultValue = "ALL") StatusFilter status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<GetFeedSummaryResponseDto> response = transactionFeedService.getMyFeeds(
                customUserDetailsDto,
                filter,
                status,
                pageable
        );
        return BaseResponseDto.success(response);
    }

    @PostMapping
    @Operation(summary = "판매글 등록", description = """
            ## 로그인한 사용자가 판매자가 되어 판매글을 등록합니다.
            
            ***
            
            ### 📥 요청 바디 (Request Body)
            ```json
            {
              "title": "SKT 10GB 저렴하게 판매합니다",
              "content": "데이터 급하게 판매합니다. 연락주세요.",
              "salesTypeId": 1,
              "telecomCompanyId": 1,
              "salesPrice": 15000,
              "salesDataAmount": 10000,
              "defaultImageNumber": 1
            }
            ```
            
            ### 📥 요청 바디 필드 설명
            * `title`, `content`: 제목 및 내용 (문자열)
            * `salesTypeId`: 판매 유형 ID (숫자, 1: 일반 판매, 2: 입찰 판매)
            * `telecomCompanyId`: 통신사 ID (숫자, 1: SKT, 2: KT, 3: LG U+)
            * `salesPrice`: 판매 가격 (숫자, 원 단위)
            * `salesDataAmount`: 판매 데이터양 (숫자, MB 단위)
            * `defaultImageNumber`: 기본 이미지 번호 (숫자)
            
            ### 🔑 권한
            * `ROLE_USER` (사용자 로그인 필요)
            
            ### ❌ 주요 실패 코드
            * `30005` (SALES_TYPE_NOT_FOUND): 유효하지 않은 판매 유형 ID인 경우
            * `30004` (AUCTION_FEED_CREATE_FAIL): 입찰 판매글 등록 제한 시간(23:30 이후)에 등록 시도 시
            * `20058` (USER_DATA_NOT_FOUND): 사용자의 데이터 정보가 없는 경우
            * `20054` (USER_SELLABLE_DATA_LACK): 판매하려는 데이터보다 판매 가능 데이터가 부족한 경우
            * `60003` (TELECOM_COMPANY_NOT_FOUND): 유효하지 않은 통신사 ID인 경우
            * `60001` (INVALID_TELECOM_COMPANY): 가입된 통신사와 판매글의 통신사가 다른 경우
            """)
    public BaseResponseDto<CreateFeedResponseDto> createFeed(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @RequestBody CreateFeedRequestDto createFeedRequestDto
    ) {
        CreateFeedResponseDto createFeedResponse = transactionFeedService.createFeed(customUserDetailsDto.getEmail(), createFeedRequestDto);
        return BaseResponseDto.success(createFeedResponse);
    }

    @PutMapping
    @Operation(summary = "판매글 수정", description = """
            ## 로그인한 사용자가 자신의 판매글을 수정합니다.
            
            ***
            
            ### 📥 요청 바디 (Request Body)
            ```json
            {
              "transactionFeedId": 123,
              "title": "수정된 제목입니다",
              "content": "내용도 수정했어요.",
              "salesPrice": 14000,
              "salesDataAmount": 10000,
              "defaultImageNumber": 2
            }
            ```
            
            ### 📥 요청 바디 필드 설명
            * `transactionFeedId`: 수정할 판매글의 ID (숫자)
            * 이외 필드는 등록 API와 동일
            
            ### 🔑 권한
            * `ROLE_USER` (사용자 로그인 필요)
            
            ### ❌ 주요 실패 코드
            * `30003` (TRANSACTION_FEED_NOT_FOUND): 수정할 판매글이 없는 경우
            * `30002` (FEED_MODIFY_PERMISSION_DENIED): 판매글 소유주가 아닌 경우
            * `30006` (AUCTION_FEED_MODIFY_NOT_ALLOWED): 입찰 판매글은 수정할 수 없는 경우
            * `20054` (USER_SELLABLE_DATA_LACK): 수정한 데이터가 판매가능 데이터보다 많은 경우
            * `20058` (USER_DATA_NOT_FOUND): 사용자의 데이터 정보가 없는 경우
            * `20055` (SELLABLE_DATA_DEDUCT_FAIL):  판매 가능 데이터 차감 도중 오류가 발생한 경우
            """)
    public BaseResponseDto<UpdateFeedResponseDto> updateFeed(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @RequestBody UpdateFeedRequestDto updateFeedRequestDto
    ) {
        UpdateFeedResponseDto updateFeedResponse = transactionFeedService.updateFeed(customUserDetailsDto.getEmail(), updateFeedRequestDto);
        return BaseResponseDto.success(updateFeedResponse);
    }

    @DeleteMapping("/{transactionFeedId}")
    @Operation(summary = "판매글 삭제", description = """
            ## 로그인한 사용자가 자신의 판매글을 삭제합니다.
            
            ***
            
            ### 📥 요청 파라미터 (Path Variable)
            | 이름 | 타입 | 필수 | 설명 |
            |---|---|:---:|---|
            | `transactionFeedId` | `Long` | O | 삭제할 판매글의 ID |
            
            ### 🔑 권한
            * `ROLE_USER` (사용자 로그인 필요)
            
            ### ❌ 주요 실패 코드
            * `30003` (TRANSACTION_FEED_NOT_FOUND): 삭제할 판매글이 없는 경우
            * `30002` (FEED_MODIFY_PERMISSION_DENIED): 판매글 소유주가 아닌 경우
            * `30006` (AUCTION_FEED_MODIFY_NOT_ALLOWED): 입찰 판매글은 삭제할 수 없는 경우
            """)
    public BaseResponseDto<Void> deleteFeed(
            @PathVariable Long transactionFeedId,
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto
    ) {
        transactionFeedService.deleteFeed(customUserDetailsDto.getEmail(), transactionFeedId);
        return BaseResponseDto.voidSuccess();
    }
}
