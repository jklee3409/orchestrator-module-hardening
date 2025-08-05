package eureca.capstone.project.orchestrator.event.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QQuizParticipation is a Querydsl query type for QuizParticipation
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQuizParticipation extends EntityPathBase<QuizParticipation> {

    private static final long serialVersionUID = -1151683434L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QQuizParticipation quizParticipation = new QQuizParticipation("quizParticipation");

    public final QQuiz quiz;

    public final NumberPath<Long> quizParticipationId = createNumber("quizParticipationId", Long.class);

    public final NumberPath<Long> reward = createNumber("reward", Long.class);

    public final eureca.capstone.project.orchestrator.common.entity.QStatus status;

    public final eureca.capstone.project.orchestrator.user.entity.QUser user;

    public QQuizParticipation(String variable) {
        this(QuizParticipation.class, forVariable(variable), INITS);
    }

    public QQuizParticipation(Path<? extends QuizParticipation> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QQuizParticipation(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QQuizParticipation(PathMetadata metadata, PathInits inits) {
        this(QuizParticipation.class, metadata, inits);
    }

    public QQuizParticipation(Class<? extends QuizParticipation> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.quiz = inits.isInitialized("quiz") ? new QQuiz(forProperty("quiz")) : null;
        this.status = inits.isInitialized("status") ? new eureca.capstone.project.orchestrator.common.entity.QStatus(forProperty("status")) : null;
        this.user = inits.isInitialized("user") ? new eureca.capstone.project.orchestrator.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

