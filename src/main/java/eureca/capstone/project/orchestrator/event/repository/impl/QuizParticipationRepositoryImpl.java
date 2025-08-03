package eureca.capstone.project.orchestrator.event.repository.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.event.dto.request.ModifyQuizStatusRequestDto;
import eureca.capstone.project.orchestrator.event.entity.QuizParticipation;
import eureca.capstone.project.orchestrator.event.repository.custom.QuizParticipationRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static eureca.capstone.project.orchestrator.event.entity.QQuizParticipation.quizParticipation;

@Repository
@RequiredArgsConstructor
public class QuizParticipationRepositoryImpl implements QuizParticipationRepositoryCustom {
    private final JPAQueryFactory queryFactory;


    @Override
    public Optional<QuizParticipation> findQuizParticipationInfo(Long userId, ModifyQuizStatusRequestDto modifyQuizStatusRequestDto) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(quizParticipation)
                        .where(
                                quizParticipation.user.userId.eq(userId),
                                quizParticipation.quiz.quizId.eq(modifyQuizStatusRequestDto.getQuizId())
                        )
                        .fetchOne()
        );
    }

    @Override
    public void updateQuizParticipationStatus(Long userId, Long reward, Status status, ModifyQuizStatusRequestDto modifyQuizStatusRequestDto) {
        queryFactory.update(quizParticipation)
                .set(quizParticipation.status, status)
                .set(quizParticipation.reward, reward)
                .where(
                        quizParticipation.user.userId.eq(userId),
                        quizParticipation.quiz.quizId.eq(modifyQuizStatusRequestDto.getQuizId())
                )
                .execute();
    }
}
