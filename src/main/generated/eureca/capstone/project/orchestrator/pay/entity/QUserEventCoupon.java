package eureca.capstone.project.orchestrator.pay.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserEventCoupon is a Querydsl query type for UserEventCoupon
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserEventCoupon extends EntityPathBase<UserEventCoupon> {

    private static final long serialVersionUID = 365306673L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserEventCoupon userEventCoupon = new QUserEventCoupon("userEventCoupon");

    public final eureca.capstone.project.orchestrator.common.entity.QBaseEntity _super = new eureca.capstone.project.orchestrator.common.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final QEventCoupon eventCoupon;

    public final DateTimePath<java.time.LocalDateTime> expiresAt = createDateTime("expiresAt", java.time.LocalDateTime.class);

    public final eureca.capstone.project.orchestrator.common.entity.QStatus status;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final eureca.capstone.project.orchestrator.user.entity.QUser user;

    public final NumberPath<Long> userEventCouponId = createNumber("userEventCouponId", Long.class);

    public QUserEventCoupon(String variable) {
        this(UserEventCoupon.class, forVariable(variable), INITS);
    }

    public QUserEventCoupon(Path<? extends UserEventCoupon> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserEventCoupon(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserEventCoupon(PathMetadata metadata, PathInits inits) {
        this(UserEventCoupon.class, metadata, inits);
    }

    public QUserEventCoupon(Class<? extends UserEventCoupon> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.eventCoupon = inits.isInitialized("eventCoupon") ? new QEventCoupon(forProperty("eventCoupon"), inits.get("eventCoupon")) : null;
        this.status = inits.isInitialized("status") ? new eureca.capstone.project.orchestrator.common.entity.QStatus(forProperty("status")) : null;
        this.user = inits.isInitialized("user") ? new eureca.capstone.project.orchestrator.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

