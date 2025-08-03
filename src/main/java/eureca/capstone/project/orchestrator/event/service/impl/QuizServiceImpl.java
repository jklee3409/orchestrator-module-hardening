package eureca.capstone.project.orchestrator.event.service.impl;

import eureca.capstone.project.orchestrator.common.component.RewardSelector;
import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.common.exception.custom.UserNotFoundException;
import eureca.capstone.project.orchestrator.common.service.QuizAiService;
import eureca.capstone.project.orchestrator.common.util.StatusManager;
import eureca.capstone.project.orchestrator.event.dto.reponse.GetTodayQuizResponseDto;
import eureca.capstone.project.orchestrator.event.dto.reponse.ModifyQuizStatusResponseDto;
import eureca.capstone.project.orchestrator.event.dto.request.ModifyQuizStatusRequestDto;
import eureca.capstone.project.orchestrator.event.entity.Quiz;
import eureca.capstone.project.orchestrator.event.repository.QuizParticipationRepository;
import eureca.capstone.project.orchestrator.event.repository.QuizRepository;
import eureca.capstone.project.orchestrator.event.service.QuizService;
import eureca.capstone.project.orchestrator.pay.service.UserPayService;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {
    private final QuizAiService quizAiService;
    private final QuizRepository quizRepository;
    private final QuizParticipationRepository quizParticipationRepository;
    private final StatusManager statusManager;
    private final UserPayService userPayService;
    private final UserRepository userRepository;
    private final RewardSelector rewardSelector;

    @Override
    public GetTodayQuizResponseDto getTodayQuiz() {
        // 퀴즈 조회 시도
        Optional<Quiz> todayQuiz = quizRepository.findTodayQuiz();
        // 비 존재 하는 경우
        if (todayQuiz.isEmpty()) {
            // AI를 통해서 새롭게 퀴즈 조회
            Quiz quiz = Quiz.fromQuizDto(quizAiService.generateQuiz());
            Quiz saveQuiz = quizRepository.save(quiz);
            return GetTodayQuizResponseDto.fromQuiz(saveQuiz);
        }
        // 퀴즈가 존재한다면, 조회된 dto 반환
        return GetTodayQuizResponseDto.fromQuiz(todayQuiz.get());
    }

    @Override
    public ModifyQuizStatusResponseDto modifyQuizStatusByEvent(Long userId, ModifyQuizStatusRequestDto modifyQuizStatusRequestDto) {
        // TODO 예외처리 추가 예정
        quizParticipationRepository.findQuizParticipationInfo(userId, modifyQuizStatusRequestDto).orElseThrow(
                () -> new RuntimeException("Quiz participation not found")
        );

        // 랜덤한 포인트 값으로 user_data + 변경
        long reward = rewardSelector.selectTodayReward(userId);
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        userPayService.charge(user, reward);

        // 상태값을 완료로 변경 처리함
        Status getStatus = statusManager.getStatus("EVENT", "DONE");
        quizParticipationRepository.updateQuizParticipationStatus(userId, reward, getStatus, modifyQuizStatusRequestDto);

        ModifyQuizStatusResponseDto modifyQuizStatusResponseDto = ModifyQuizStatusResponseDto.builder()
                .userId(userId)
                .quizId(modifyQuizStatusRequestDto.getQuizId())
                .reward(reward)
                .build();

        log.info("[modifyQuizStatusByEvent] modifyQuizStatusResponseDto : {}", modifyQuizStatusResponseDto);
        return modifyQuizStatusResponseDto;
    }
}
