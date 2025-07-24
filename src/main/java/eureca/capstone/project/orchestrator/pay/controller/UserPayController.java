package eureca.capstone.project.orchestrator.pay.controller;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.pay.dto.response.GetPayBalanceResponseDto;
import eureca.capstone.project.orchestrator.pay.service.UserPayService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orchestrator/user-pay")
@RequiredArgsConstructor
public class UserPayController {
    private final UserPayService userPayService;

    @GetMapping
    @Operation(summary = "사용자 페이 잔액 조회 API", description = "사용자의 페이 잔액을 조회합니다. ")
    public BaseResponseDto<GetPayBalanceResponseDto> getPayBalance(
        @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto
    ) {
        GetPayBalanceResponseDto responseDto = userPayService.getPay(customUserDetailsDto.getEmail());
        return BaseResponseDto.success(responseDto);
    }
}
