package eureca.capstone.project.orchestrator.event.repository;

import eureca.capstone.project.orchestrator.event.entity.QuizParticipation;
import eureca.capstone.project.orchestrator.event.repository.custom.QuizParticipationRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizParticipationRepository extends JpaRepository<QuizParticipation, Long>, QuizParticipationRepositoryCustom {
}
