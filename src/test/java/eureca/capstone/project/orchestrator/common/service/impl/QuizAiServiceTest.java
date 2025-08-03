package eureca.capstone.project.orchestrator.common.service.impl;

import eureca.capstone.project.orchestrator.common.service.QuizAiService;
import eureca.capstone.project.orchestrator.event.dto.reponse.GetTodayQuizResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class QuizAiServiceTest {

    @Autowired
    QuizAiService quizAiService;

    @Test
    @DisplayName("정상적으로 퀴즈가 생성되는지 확인한다.")
    public void generateQuiz() {
        // given, when
        GetTodayQuizResponseDto getTodayQuizResponseDto = quizAiService.generateQuiz();

        // then
        assertThat(getTodayQuizResponseDto).isNotNull();
    }
}