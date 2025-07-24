package eureca.capstone.project.orchestrator.user.controller;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.user.dto.request.user_data.UpdateUserDataRequestDto;
import eureca.capstone.project.orchestrator.user.dto.response.user_data.CreateSellableDataResponseDto;
import eureca.capstone.project.orchestrator.user.dto.response.user_data.GetUserDataStatusResponseDto;
import eureca.capstone.project.orchestrator.user.service.UserDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "사용자 데이터 API", description = "사용자 데이터 조회 및 판매 가능 데이터 전환 API")
@RestController
@RequestMapping("/orchestrator/user-data")
@RequiredArgsConstructor
public class UserDataController {
    private final UserDataService userDataService;

    @GetMapping("/status")
    @Operation(summary = "사용자 데이터 현황 조회", description = """
    ## 로그인한 사용자의 보유 데이터, 판매 가능 데이터, 구매 데이터 현황을 조회합니다.
    
    ***
    
    ### 🔑 권한
    * `ROLE_USER` (사용자 로그인 필요)
    
    ### ❌ 주요 실패 코드
    * `20000` (USER_NOT_FOUND): 사용자를 찾을 수 없는 경우
    * `20058` (USER_DATA_NOT_FOUND): 사용자의 데이터 정보가 존재하지 않는 경우
    """)
    public BaseResponseDto<GetUserDataStatusResponseDto> getUserDataStatus(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        GetUserDataStatusResponseDto responseDto = userDataService.getUserDataStatus(userDetails.getUsername());
        return BaseResponseDto.success(responseDto);
    }

    @PutMapping("/enable-sale/change")
    @Operation(summary = "보유 데이터 → 판매 가능 데이터 전환", description = """
    ## 로그인한 사용자의 현재 보유 데이터를 판매가 가능한 데이터로 전환합니다.
    
    ***
    
    ### 📥 요청 바디 (Request Body)
    ```json
    {
      "amount": 1000
    }
    ```
    
    ### 📥 요청 바디 필드 설명
    * `amount`: 판매 가능 데이터로 전환할 데이터 양 (숫자, MB 단위)
    
    ### 🔑 권한
    * `ROLE_USER` (사용자 로그인 필요)
    
    ### ❌ 주요 실패 코드
    * `20000` (USER_NOT_FOUND): 사용자를 찾을 수 없는 경우
    * `20052` (USER_TOTAL_DATA_LACK): 전환하려는 양보다 보유 데이터가 부족한 경우
    * `20053` (SELLABLE_DATA_CREATE_FAIL): 데이터 전환 처리 중 오류가 발생한 경우
    """)
    public BaseResponseDto<CreateSellableDataResponseDto> createSellableData(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @RequestBody UpdateUserDataRequestDto requestDto
    ) {
        CreateSellableDataResponseDto createSellableDataResponseDto = userDataService.createSellableData(customUserDetailsDto.getEmail(), requestDto);
        return BaseResponseDto.success(createSellableDataResponseDto);
    }
}
