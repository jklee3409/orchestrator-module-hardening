package eureca.capstone.project.orchestrator.transaction_feed.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserDataCoupon is a Querydsl query type for UserDataCoupon
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserDataCoupon extends EntityPathBase<UserDataCoupon> {

    private static final long serialVersionUID = -2110751464L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserDataCoupon userDataCoupon = new QUserDataCoupon("userDataCoupon");

    public final eureca.capstone.project.orchestrator.common.entity.QBaseEntity _super = new eureca.capstone.project.orchestrator.common.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final QDataCoupon dataCoupon;

    public final DateTimePath<java.time.LocalDateTime> expiresAt = createDateTime("expiresAt", java.time.LocalDateTime.class);

    public final eureca.capstone.project.orchestrator.common.entity.QStatus status;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final eureca.capstone.project.orchestrator.user.entity.QUser user;

    public final NumberPath<Long> userDataCouponId = createNumber("userDataCouponId", Long.class);

    public QUserDataCoupon(String variable) {
        this(UserDataCoupon.class, forVariable(variable), INITS);
    }

    public QUserDataCoupon(Path<? extends UserDataCoupon> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserDataCoupon(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserDataCoupon(PathMetadata metadata, PathInits inits) {
        this(UserDataCoupon.class, metadata, inits);
    }

    public QUserDataCoupon(Class<? extends UserDataCoupon> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.dataCoupon = inits.isInitialized("dataCoupon") ? new QDataCoupon(forProperty("dataCoupon"), inits.get("dataCoupon")) : null;
        this.status = inits.isInitialized("status") ? new eureca.capstone.project.orchestrator.common.entity.QStatus(forProperty("status")) : null;
        this.user = inits.isInitialized("user") ? new eureca.capstone.project.orchestrator.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

