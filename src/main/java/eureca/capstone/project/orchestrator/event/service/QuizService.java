package eureca.capstone.project.orchestrator.event.service;

import eureca.capstone.project.orchestrator.event.dto.reponse.GetTodayQuizResponseDto;
import eureca.capstone.project.orchestrator.event.dto.reponse.ModifyQuizStatusResponseDto;
import eureca.capstone.project.orchestrator.event.dto.request.ModifyQuizStatusRequestDto;

public interface QuizService {
    GetTodayQuizResponseDto getTodayQuiz();

    ModifyQuizStatusResponseDto modifyQuizStatusByEvent(Long userId, ModifyQuizStatusRequestDto modifyQuizStatusRequestDto);
}
