package eureca.capstone.project.orchestrator.event.repository;

import eureca.capstone.project.orchestrator.event.entity.Quiz;
import eureca.capstone.project.orchestrator.event.repository.custom.QuizRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long>, QuizRepositoryCustom {
}
