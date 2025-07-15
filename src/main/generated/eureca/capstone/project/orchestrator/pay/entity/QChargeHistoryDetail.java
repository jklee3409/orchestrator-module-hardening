package eureca.capstone.project.orchestrator.pay.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QChargeHistoryDetail is a Querydsl query type for ChargeHistoryDetail
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChargeHistoryDetail extends EntityPathBase<ChargeHistoryDetail> {

    private static final long serialVersionUID = -740862387L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QChargeHistoryDetail chargeHistoryDetail = new QChargeHistoryDetail("chargeHistoryDetail");

    public final eureca.capstone.project.orchestrator.common.entity.QBaseEntity _super = new eureca.capstone.project.orchestrator.common.entity.QBaseEntity(this);

    public final QChargeHistory chargeHistory;

    public final NumberPath<Long> chargeHistoryDetailId = createNumber("chargeHistoryDetailId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final QPayHistory payHistory;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QChargeHistoryDetail(String variable) {
        this(ChargeHistoryDetail.class, forVariable(variable), INITS);
    }

    public QChargeHistoryDetail(Path<? extends ChargeHistoryDetail> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QChargeHistoryDetail(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QChargeHistoryDetail(PathMetadata metadata, PathInits inits) {
        this(ChargeHistoryDetail.class, metadata, inits);
    }

    public QChargeHistoryDetail(Class<? extends ChargeHistoryDetail> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.chargeHistory = inits.isInitialized("chargeHistory") ? new QChargeHistory(forProperty("chargeHistory"), inits.get("chargeHistory")) : null;
        this.payHistory = inits.isInitialized("payHistory") ? new QPayHistory(forProperty("payHistory"), inits.get("payHistory")) : null;
    }

}

