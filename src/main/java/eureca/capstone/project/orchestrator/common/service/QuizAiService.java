package eureca.capstone.project.orchestrator.common.service;

import eureca.capstone.project.orchestrator.event.dto.reponse.GetTodayQuizResponseDto;

public interface QuizAiService {
    GetTodayQuizResponseDto generateQuiz();
}
