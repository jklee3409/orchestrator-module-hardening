package eureca.capstone.project.orchestrator.pay.controller;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.pay.dto.BankDto;
import eureca.capstone.project.orchestrator.pay.dto.request.ExchangeRequestDto;
import eureca.capstone.project.orchestrator.pay.dto.response.GetPayBalanceResponseDto;
import eureca.capstone.project.orchestrator.pay.service.ExchangeService;
import eureca.capstone.project.orchestrator.pay.service.UserPayService;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "사용자 페이 API", description = "사용자 페이 조회, 환전 등 API")
@RestController
@RequestMapping("/orchestrator/user-pay")
@RequiredArgsConstructor
public class UserPayController {
    private final UserPayService userPayService;
    private final ExchangeService exchangeService;

    @GetMapping("/banks")
    @Operation(summary = "은행 목록 조회", description = """
            ## 환전 가능한 은행 목록 전체를 조회합니다.
            
            ***
            
            ### 🔑 권한
            * 'ROLE_USER'(사용자 로그인 필요)
            """)
    public BaseResponseDto<List<BankDto>> getBankList() {
        List<BankDto> bankList = exchangeService.getBankList();
        return BaseResponseDto.success(bankList);
    }

    @GetMapping
    @Operation(summary = "사용자 페이 잔액 조회", description = """
            ## 로그인한 사용자의 현재 페이 잔액을 조회합니다.
            
            ***
            
            ### 🔑 권한
            * `ROLE_USER` (사용자 로그인 필요)
            
            ### ❌ 주요 실패 코드
            * `20000` (USER_NOT_FOUND): 사용자를 찾을 수 없는 경우
            """)
    public BaseResponseDto<GetPayBalanceResponseDto> getPayBalance(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto
    ) {
        GetPayBalanceResponseDto responseDto = userPayService.getPay(customUserDetailsDto.getEmail());
        return BaseResponseDto.success(responseDto);
    }

    @PostMapping("/exchange")
    @Operation(summary = "페이 환전 요청", description = """
            ## 사용자의 페이를 지정된 은행 계좌로 환전 요청합니다.
            환전 시 3%의 수수료가 차감됩니다.
            
            ***
            
            ### 📥 요청 바디 (Request Body)
            ```json
            {
              "bankId": 1,
              "exchangeAccount": "123-456-789012",
              "amount": 10000
            }
            ```
            
            ### 📥 요청 바디 필드 설명
            * `bankId`: 환전할 은행의 ID (숫자)
            * `exchangeAccount`: 환전받을 계좌번호 (문자열)
            * `amount`: 환전할 페이 금액 (숫자)
            
            ### 🔑 권한
            * `ROLE_USER` (사용자 로그인 필요)
            
            ### ❌ 주요 실패 코드
            * `20000` (USER_NOT_FOUND): 사용자를 찾을 수 없는 경우
            * `40059` (BANK_NOT_FOUND): 유효하지 않은 은행 ID인 경우
            * `40057` (USER_PAY_LACK): 환전하려는 금액보다 보유 페이가 부족한 경우
            """)
    public BaseResponseDto<Void> exchangePay(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @RequestBody ExchangeRequestDto requestDto
    ) {
        exchangeService.exchangePay(customUserDetailsDto.getEmail(), requestDto);
        return BaseResponseDto.voidSuccess();
    }
}
