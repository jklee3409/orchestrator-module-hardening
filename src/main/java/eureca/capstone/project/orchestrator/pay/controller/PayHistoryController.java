package eureca.capstone.project.orchestrator.pay.controller;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.pay.dto.PayHistoryDto.PayHistorySimpleDto;
import eureca.capstone.project.orchestrator.pay.dto.response.PayHistoryDetailResponseDto;
import eureca.capstone.project.orchestrator.pay.service.PayHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orchestrator/pay-history")
@RequiredArgsConstructor
public class PayHistoryController {
    private final PayHistoryService payHistoryService;

    @GetMapping
    @Operation(summary = "페이 변동 내역 조회 API", description = "사용자의 페이 변동 내역을 조회합니다.")
    public BaseResponseDto<Page<PayHistorySimpleDto>> getPayHistoryList(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<PayHistorySimpleDto> payHistoryList = payHistoryService.getPayHistoryList(customUserDetailsDto.getEmail(), pageable);
        return BaseResponseDto.success(payHistoryList);
    }

    @GetMapping("/{payHistoryId}")
    @Operation(summary = "페이 변동 내역 상세 조회 API", description = "사용자의 특정 페이 변동 내역을 상세 조회합니다.")
    public BaseResponseDto<PayHistoryDetailResponseDto> getPayHistoryDetail(
            @AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
           @PathVariable Long payHistoryId
    ) {
        PayHistoryDetailResponseDto payHistoryDetail = payHistoryService.getPayHistoryDetail(customUserDetailsDto.getEmail(), payHistoryId);
        return BaseResponseDto.success(payHistoryDetail);
    }
}
