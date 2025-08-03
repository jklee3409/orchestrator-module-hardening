package eureca.capstone.project.orchestrator.event.dto.reponse;

import eureca.capstone.project.orchestrator.event.entity.Quiz;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetTodayQuizResponseDto {
    private Long quizId;
    private String quizTitle;
    private String quizDescription;
    private String quizAnswer;
    private String quizHint;

    public static GetTodayQuizResponseDto fromQuiz(Quiz quiz) {
        return GetTodayQuizResponseDto.builder()
                .quizId(quiz.getQuizId())
                .quizTitle(quiz.getQuizTitle())
                .quizDescription(quiz.getQuizDescription())
                .quizAnswer(quiz.getQuizAnswer())
                .quizHint(quiz.getQuizHint())
                .build();
    }
}
