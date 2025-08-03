package eureca.capstone.project.orchestrator.event.repository.custom;

import eureca.capstone.project.orchestrator.event.entity.Quiz;

import java.util.Optional;

public interface QuizRepositoryCustom {
    Optional<Quiz> findTodayQuiz();
}
