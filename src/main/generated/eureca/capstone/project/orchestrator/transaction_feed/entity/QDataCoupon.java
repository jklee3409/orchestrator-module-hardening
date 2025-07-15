package eureca.capstone.project.orchestrator.transaction_feed.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QDataCoupon is a Querydsl query type for DataCoupon
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDataCoupon extends EntityPathBase<DataCoupon> {

    private static final long serialVersionUID = 2057469293L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QDataCoupon dataCoupon = new QDataCoupon("dataCoupon");

    public final eureca.capstone.project.orchestrator.common.entity.QBaseEntity _super = new eureca.capstone.project.orchestrator.common.entity.QBaseEntity(this);

    public final StringPath couponNumber = createString("couponNumber");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> dataAmount = createNumber("dataAmount", Long.class);

    public final NumberPath<Long> dataCouponId = createNumber("dataCouponId", Long.class);

    public final eureca.capstone.project.orchestrator.common.entity.QTelecomCompany telecomCompany;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QDataCoupon(String variable) {
        this(DataCoupon.class, forVariable(variable), INITS);
    }

    public QDataCoupon(Path<? extends DataCoupon> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QDataCoupon(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QDataCoupon(PathMetadata metadata, PathInits inits) {
        this(DataCoupon.class, metadata, inits);
    }

    public QDataCoupon(Class<? extends DataCoupon> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.telecomCompany = inits.isInitialized("telecomCompany") ? new eureca.capstone.project.orchestrator.common.entity.QTelecomCompany(forProperty("telecomCompany")) : null;
    }

}

