package eureca.capstone.project.orchestrator.event.dto.reponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ModifyQuizStatusResponseDto {
    private Long userId;
    private Long quizId;
    private Long reward;
}
