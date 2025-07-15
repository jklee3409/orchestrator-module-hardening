package eureca.capstone.project.orchestrator.user.controller;

import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.user.dto.request.user_data.GetUserDataStatusRequestDto;
import eureca.capstone.project.orchestrator.user.dto.response.user_data.GetUserDataStatusResponseDto;
import eureca.capstone.project.orchestrator.user.service.UserDataService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user-data")
@RequiredArgsConstructor
public class UserDataController {
    private final UserDataService userDataService;

    @GetMapping("/status")
    @Operation(summary = "사용자 데이터 현황 조회", description = "userId 에 해당하는 사용자의 보유 데이터, 판매 가능 데이터, 구매 데이터를 반환합니다.")
    public BaseResponseDto<GetUserDataStatusResponseDto> getUserDataStatus(@Valid GetUserDataStatusRequestDto requestDto) {
        GetUserDataStatusResponseDto responseDto = userDataService.getUserDataStatus(requestDto);
        return BaseResponseDto.success(responseDto);
    }
}
