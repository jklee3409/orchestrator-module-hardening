package eureca.capstone.project.orchestrator.event.entity;

import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "quiz_participation")
public class QuizParticipation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_participation_id")
    private Long quizParticipationId;

    @JoinColumn(name = "quiz")
    @ManyToOne(fetch = FetchType.LAZY)
    private Quiz quiz;

    @JoinColumn(name = "user")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private Long reward;

    @ManyToOne(fetch = FetchType.LAZY)
    private Status status;
}
