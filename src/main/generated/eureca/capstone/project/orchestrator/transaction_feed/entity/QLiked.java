package eureca.capstone.project.orchestrator.transaction_feed.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QLiked is a Querydsl query type for Liked
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLiked extends EntityPathBase<Liked> {

    private static final long serialVersionUID = 1737894032L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QLiked liked = new QLiked("liked");

    public final eureca.capstone.project.orchestrator.common.entity.QBaseEntity _super = new eureca.capstone.project.orchestrator.common.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> likedId = createNumber("likedId", Long.class);

    public final QTransactionFeed transactionFeed;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final eureca.capstone.project.orchestrator.user.entity.QUser user;

    public QLiked(String variable) {
        this(Liked.class, forVariable(variable), INITS);
    }

    public QLiked(Path<? extends Liked> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QLiked(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QLiked(PathMetadata metadata, PathInits inits) {
        this(Liked.class, metadata, inits);
    }

    public QLiked(Class<? extends Liked> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.transactionFeed = inits.isInitialized("transactionFeed") ? new QTransactionFeed(forProperty("transactionFeed"), inits.get("transactionFeed")) : null;
        this.user = inits.isInitialized("user") ? new eureca.capstone.project.orchestrator.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

