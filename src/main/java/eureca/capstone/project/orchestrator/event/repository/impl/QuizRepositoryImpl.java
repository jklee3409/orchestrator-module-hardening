package eureca.capstone.project.orchestrator.event.repository.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import eureca.capstone.project.orchestrator.event.entity.Quiz;
import eureca.capstone.project.orchestrator.event.repository.custom.QuizRepositoryCustom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static eureca.capstone.project.orchestrator.event.entity.QQuiz.quiz;

@Slf4j
@Repository
@RequiredArgsConstructor
public class QuizRepositoryImpl implements QuizRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Quiz> findTodayQuiz() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();              // 오늘 00:00:00
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        return Optional.ofNullable(
                queryFactory.selectFrom(quiz)
                        .where(
                                quiz.createdAt.between(
                                        startOfDay,
                                        endOfDay.minusNanos(1)
                                )
                        )
                        .fetchOne()
        );

    }
}
