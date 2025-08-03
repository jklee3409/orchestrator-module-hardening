package eureca.capstone.project.orchestrator.event.controller;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.event.dto.reponse.GetTodayQuizResponseDto;
import eureca.capstone.project.orchestrator.event.dto.reponse.ModifyQuizStatusResponseDto;
import eureca.capstone.project.orchestrator.event.dto.request.ModifyQuizStatusRequestDto;
import eureca.capstone.project.orchestrator.event.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "퀴즈 API", description = "오늘의 퀴즈 조회 및 참여 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/orchestrator/event")
public class QuizController {
    private final QuizService quizService;

    @Operation(summary = "오늘의 퀴즈 조회 API", description = """
            ## 오늘의 퀴즈 내용과 정답을 조회합니다.
            
            ***
            
            ### 📥 요청 파라미터
            * 별도의 요청 파라미터는 없습니다.
            
            ### 🔑 권한
            * `permitAll` (로그인 없이 접근 가능)
            
            ### ❌ 주요 실패 코드
            
            ### 📝 참고 사항
            * 매일 자정 기준으로 새로운 퀴즈가 제공됩니다.
            * 만약 오늘 생성된 퀴즈가 없다면, AI를 통해 새로운 퀴즈를 생성하여 반환합니다.
            """)
    @GetMapping("/quizzes")
    public BaseResponseDto<GetTodayQuizResponseDto> getQuizzesByEvent() {
        log.info("[getQuizzesByEvent] start");
        GetTodayQuizResponseDto getTodayQuizResponseDto = quizService.getTodayQuiz();
        BaseResponseDto<GetTodayQuizResponseDto> success = BaseResponseDto.success(getTodayQuizResponseDto);
        log.info("[getQuizzesByEvent] success : {}", success);
        return success;
    }

    @Operation(summary = "퀴즈 참여 및 보상 지급 API", description = """
            ## 사용자가 퀴즈에 참여하고 정답을 맞혔을 때 호출하여 참여 상태를 변경하고 보상을 지급합니다.
            
            ***
            
            ### 📥 요청 바디 (Request Body)
            ```json
            {
              "quizId": 1
            }
            ```
            
            ### 🔑 권한
            * `ROLE_USER` (사용자 로그인 필요)
            
            ### ❌ 주요 실패 코드
            * `20000` (USER_NOT_FOUND): 유효하지 않은 토큰으로 요청하여 사용자를 찾을 수 없을 경우 발생합니다.
            * `60005` (QUIZ_ALREADY_PARTICIPATED): 사용자가 이미 해당 퀴즈에 참여하여 보상을 받았을 경우 발생합니다.
            * `60006` (QUIZ_NOT_FOUND): 요청한 ID에 해당하는 퀴즈를 찾을 수 없을 경우 발생합니다.
            
            ### 📝 참고 사항
            * 사용자가 퀴즈 페이지에 처음 접근할 때 'PENDING' 상태로 참여 기록이 생성될 수 있으며, 정답을 맞힌 후 이 API를 호출하면 'DONE' 상태로 변경되고 랜덤한 페이 보상이 지급됩니다.
            * 보상 금액은 서버에서 랜덤하게 결정됩니다.
            """)
    @PostMapping("/modify-quiz-status")
    public BaseResponseDto<ModifyQuizStatusResponseDto> modifyQuizStatusByEvent(@AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
                                                                                @RequestBody ModifyQuizStatusRequestDto modifyQuizStatusRequestDto
    ) {
        log.info("[modifyQuizStatusByEvent] {}", modifyQuizStatusRequestDto);
        Long userId = customUserDetailsDto.getUserId();
        ModifyQuizStatusResponseDto modifyQuizStatusResponseDto = quizService.modifyQuizStatusByEvent(userId, modifyQuizStatusRequestDto);
        log.info("[modifyQuizStatusByEvent] modifyQuizStatusResponseDto : {}", modifyQuizStatusResponseDto);
        BaseResponseDto<ModifyQuizStatusResponseDto> success = BaseResponseDto.success(modifyQuizStatusResponseDto);
        log.info("[modifyQuizStatusByEvent] success : {}", success);
        return success;
    }
}