package eureca.capstone.project.orchestrator.transaction_feed.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QDataTransactionHistory is a Querydsl query type for DataTransactionHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDataTransactionHistory extends EntityPathBase<DataTransactionHistory> {

    private static final long serialVersionUID = 1632847517L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QDataTransactionHistory dataTransactionHistory = new QDataTransactionHistory("dataTransactionHistory");

    public final eureca.capstone.project.orchestrator.common.entity.QBaseEntity _super = new eureca.capstone.project.orchestrator.common.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    public final QTransactionFeed transactionFeed;

    public final NumberPath<Long> transactionFinalPrice = createNumber("transactionFinalPrice", Long.class);

    public final NumberPath<Long> transactionHistoryId = createNumber("transactionHistoryId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final eureca.capstone.project.orchestrator.user.entity.QUser user;

    public QDataTransactionHistory(String variable) {
        this(DataTransactionHistory.class, forVariable(variable), INITS);
    }

    public QDataTransactionHistory(Path<? extends DataTransactionHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QDataTransactionHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QDataTransactionHistory(PathMetadata metadata, PathInits inits) {
        this(DataTransactionHistory.class, metadata, inits);
    }

    public QDataTransactionHistory(Class<? extends DataTransactionHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.transactionFeed = inits.isInitialized("transactionFeed") ? new QTransactionFeed(forProperty("transactionFeed"), inits.get("transactionFeed")) : null;
        this.user = inits.isInitialized("user") ? new eureca.capstone.project.orchestrator.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

