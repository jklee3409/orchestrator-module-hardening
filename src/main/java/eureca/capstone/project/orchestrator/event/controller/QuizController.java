package eureca.capstone.project.orchestrator.event.controller;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.event.dto.reponse.GetTodayQuizResponseDto;
import eureca.capstone.project.orchestrator.event.dto.reponse.ModifyQuizStatusResponseDto;
import eureca.capstone.project.orchestrator.event.dto.request.ModifyQuizStatusRequestDto;
import eureca.capstone.project.orchestrator.event.service.QuizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/orchestrator/event")
public class QuizController {
    private final QuizService quizService;

    /**
     * 퀴즈를 조회하는 API
     * 오늘 날짜의 문제와, 정답을 return
     */
    @GetMapping("/quizzes")
    public BaseResponseDto<GetTodayQuizResponseDto> getQuizzesByEvent() {
        log.info("[getQuizzesByEvent] start");
        GetTodayQuizResponseDto getTodayQuizResponseDto = quizService.getTodayQuiz();
        BaseResponseDto<GetTodayQuizResponseDto> success = BaseResponseDto.success(getTodayQuizResponseDto);
        log.info("[getQuizzesByEvent] success : {}", success);
        return success;
    }

    /**
     * 퀴즈의 상태를 변경하는 API (최조 이벤트 페이지 접근시에, 진행중 처리, 프론트 판단하에 정답 후엔, 완료 처리)
     * 여기서 진행중일때, 랜덤하게 지급 페이양을 정한다. (1 ~ 1,000,000)
     * void return
     */
    @GetMapping("/modify-quiz-status")
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


    /**
     * 퀴즈 참여 여부를 조회하는 API
     * 단순히 select 처리 하면 되고 > 여기서 row 생성
     * boolean return
     */
    @GetMapping("/find-quiz-participant")
    public void findQuizParticipantByEvent() {

    }
}
