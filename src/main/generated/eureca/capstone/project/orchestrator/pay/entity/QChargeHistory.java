package eureca.capstone.project.orchestrator.pay.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QChargeHistory is a Querydsl query type for ChargeHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChargeHistory extends EntityPathBase<ChargeHistory> {

    private static final long serialVersionUID = 1404167900L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QChargeHistory chargeHistory = new QChargeHistory("chargeHistory");

    public final eureca.capstone.project.orchestrator.common.entity.QBaseEntity _super = new eureca.capstone.project.orchestrator.common.entity.QBaseEntity(this);

    public final NumberPath<Long> amount = createNumber("amount", Long.class);

    public final NumberPath<Long> chargeHistoryId = createNumber("chargeHistoryId", Long.class);

    public final NumberPath<Long> chargePay = createNumber("chargePay", Long.class);

    public final DateTimePath<java.time.LocalDateTime> completedAt = createDateTime("completedAt", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> discountAmount = createNumber("discountAmount", Long.class);

    public final NumberPath<Long> finalAmount = createNumber("finalAmount", Long.class);

    public final StringPath orderId = createString("orderId");

    public final StringPath paymentKey = createString("paymentKey");

    public final QPayType payType;

    public final DateTimePath<java.time.LocalDateTime> requestedAt = createDateTime("requestedAt", java.time.LocalDateTime.class);

    public final eureca.capstone.project.orchestrator.common.entity.QStatus status;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final eureca.capstone.project.orchestrator.user.entity.QUser user;

    public final QUserEventCoupon userEventCoupon;

    public QChargeHistory(String variable) {
        this(ChargeHistory.class, forVariable(variable), INITS);
    }

    public QChargeHistory(Path<? extends ChargeHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QChargeHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QChargeHistory(PathMetadata metadata, PathInits inits) {
        this(ChargeHistory.class, metadata, inits);
    }

    public QChargeHistory(Class<? extends ChargeHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.payType = inits.isInitialized("payType") ? new QPayType(forProperty("payType")) : null;
        this.status = inits.isInitialized("status") ? new eureca.capstone.project.orchestrator.common.entity.QStatus(forProperty("status")) : null;
        this.user = inits.isInitialized("user") ? new eureca.capstone.project.orchestrator.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
        this.userEventCoupon = inits.isInitialized("userEventCoupon") ? new QUserEventCoupon(forProperty("userEventCoupon"), inits.get("userEventCoupon")) : null;
    }

}

