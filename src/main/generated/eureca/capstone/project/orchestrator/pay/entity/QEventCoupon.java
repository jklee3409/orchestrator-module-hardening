package eureca.capstone.project.orchestrator.pay.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QEventCoupon is a Querydsl query type for EventCoupon
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEventCoupon extends EntityPathBase<EventCoupon> {

    private static final long serialVersionUID = 1631244796L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QEventCoupon eventCoupon = new QEventCoupon("eventCoupon");

    public final eureca.capstone.project.orchestrator.common.entity.QBaseEntity _super = new eureca.capstone.project.orchestrator.common.entity.QBaseEntity(this);

    public final StringPath couponDescription = createString("couponDescription");

    public final StringPath couponName = createString("couponName");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> discountRate = createNumber("discountRate", Long.class);

    public final NumberPath<Long> eventCouponId = createNumber("eventCouponId", Long.class);

    public final QPayType payType;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QEventCoupon(String variable) {
        this(EventCoupon.class, forVariable(variable), INITS);
    }

    public QEventCoupon(Path<? extends EventCoupon> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QEventCoupon(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QEventCoupon(PathMetadata metadata, PathInits inits) {
        this(EventCoupon.class, metadata, inits);
    }

    public QEventCoupon(Class<? extends EventCoupon> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.payType = inits.isInitialized("payType") ? new QPayType(forProperty("payType")) : null;
    }

}

