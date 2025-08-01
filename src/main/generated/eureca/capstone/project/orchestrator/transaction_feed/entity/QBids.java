package eureca.capstone.project.orchestrator.transaction_feed.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBids is a Querydsl query type for Bids
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBids extends EntityPathBase<Bids> {

    private static final long serialVersionUID = 1579783635L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBids bids = new QBids("bids");

    public final eureca.capstone.project.orchestrator.common.entity.QBaseEntity _super = new eureca.capstone.project.orchestrator.common.entity.QBaseEntity(this);

    public final NumberPath<Long> bidAmount = createNumber("bidAmount", Long.class);

    public final NumberPath<Long> bidsId = createNumber("bidsId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> bidTime = createDateTime("bidTime", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final QTransactionFeed transactionFeed;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final eureca.capstone.project.orchestrator.user.entity.QUser user;

    public QBids(String variable) {
        this(Bids.class, forVariable(variable), INITS);
    }

    public QBids(Path<? extends Bids> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBids(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBids(PathMetadata metadata, PathInits inits) {
        this(Bids.class, metadata, inits);
    }

    public QBids(Class<? extends Bids> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.transactionFeed = inits.isInitialized("transactionFeed") ? new QTransactionFeed(forProperty("transactionFeed"), inits.get("transactionFeed")) : null;
        this.user = inits.isInitialized("user") ? new eureca.capstone.project.orchestrator.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

