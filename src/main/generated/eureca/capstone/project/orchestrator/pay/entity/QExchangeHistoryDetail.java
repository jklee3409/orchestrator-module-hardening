package eureca.capstone.project.orchestrator.pay.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QExchangeHistoryDetail is a Querydsl query type for ExchangeHistoryDetail
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QExchangeHistoryDetail extends EntityPathBase<ExchangeHistoryDetail> {

    private static final long serialVersionUID = -1473707970L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QExchangeHistoryDetail exchangeHistoryDetail = new QExchangeHistoryDetail("exchangeHistoryDetail");

    public final eureca.capstone.project.orchestrator.common.entity.QBaseEntity _super = new eureca.capstone.project.orchestrator.common.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final QExchangeHistory exchangeHistory;

    public final NumberPath<Long> exchangeHistoryDetailId = createNumber("exchangeHistoryDetailId", Long.class);

    public final QPayHistory payHistory;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QExchangeHistoryDetail(String variable) {
        this(ExchangeHistoryDetail.class, forVariable(variable), INITS);
    }

    public QExchangeHistoryDetail(Path<? extends ExchangeHistoryDetail> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QExchangeHistoryDetail(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QExchangeHistoryDetail(PathMetadata metadata, PathInits inits) {
        this(ExchangeHistoryDetail.class, metadata, inits);
    }

    public QExchangeHistoryDetail(Class<? extends ExchangeHistoryDetail> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.exchangeHistory = inits.isInitialized("exchangeHistory") ? new QExchangeHistory(forProperty("exchangeHistory"), inits.get("exchangeHistory")) : null;
        this.payHistory = inits.isInitialized("payHistory") ? new QPayHistory(forProperty("payHistory"), inits.get("payHistory")) : null;
    }

}

