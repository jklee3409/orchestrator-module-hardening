package eureca.capstone.project.orchestrator.event.repository.custom;

import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.event.dto.request.ModifyQuizStatusRequestDto;
import eureca.capstone.project.orchestrator.event.entity.QuizParticipation;

import java.util.Optional;

public interface QuizParticipationRepositoryCustom {
    Optional<QuizParticipation> findQuizParticipationInfo(Long userId, ModifyQuizStatusRequestDto modifyQuizStatusRequestDto);

    void updateQuizParticipationStatus(Long userId, Long reward, Status status, ModifyQuizStatusRequestDto modifyQuizStatusRequestDto);
}
