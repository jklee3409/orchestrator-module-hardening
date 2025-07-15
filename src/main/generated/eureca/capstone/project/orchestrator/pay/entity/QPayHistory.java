package eureca.capstone.project.orchestrator.pay.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPayHistory is a Querydsl query type for PayHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPayHistory extends EntityPathBase<PayHistory> {

    private static final long serialVersionUID = -145455952L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPayHistory payHistory = new QPayHistory("payHistory");

    public final eureca.capstone.project.orchestrator.common.entity.QBaseEntity _super = new eureca.capstone.project.orchestrator.common.entity.QBaseEntity(this);

    public final NumberPath<Long> changedPay = createNumber("changedPay", Long.class);

    public final QChangeType changeType;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> finalPay = createNumber("finalPay", Long.class);

    public final NumberPath<Long> payHistoryId = createNumber("payHistoryId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final eureca.capstone.project.orchestrator.user.entity.QUser user;

    public QPayHistory(String variable) {
        this(PayHistory.class, forVariable(variable), INITS);
    }

    public QPayHistory(Path<? extends PayHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPayHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPayHistory(PathMetadata metadata, PathInits inits) {
        this(PayHistory.class, metadata, inits);
    }

    public QPayHistory(Class<? extends PayHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.changeType = inits.isInitialized("changeType") ? new QChangeType(forProperty("changeType")) : null;
        this.user = inits.isInitialized("user") ? new eureca.capstone.project.orchestrator.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

