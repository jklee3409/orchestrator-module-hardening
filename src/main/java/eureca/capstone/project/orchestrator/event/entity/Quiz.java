package eureca.capstone.project.orchestrator.event.entity;

import eureca.capstone.project.orchestrator.common.entity.BaseEntity;
import eureca.capstone.project.orchestrator.event.dto.reponse.GetTodayQuizResponseDto;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "quiz")
public class Quiz extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_id")
    private Long quizId;

    @Column(name = "quiz_title")
    private String quizTitle;

    @Column(name = "quiz_description")
    private String quizDescription;

    @Column(name = "quiz_answer")
    private String quizAnswer;

    @Column(name = "quiz_hint")
    private String quizHint;

    public static Quiz fromQuizDto(GetTodayQuizResponseDto getTodayQuizResponseDto) {
        return Quiz.builder()
                .quizTitle(getTodayQuizResponseDto.getQuizTitle())
                .quizDescription(getTodayQuizResponseDto.getQuizDescription())
                .quizAnswer(getTodayQuizResponseDto.getQuizAnswer())
                .quizHint(getTodayQuizResponseDto.getQuizHint())
                .build();
    }
}
