package eureca.capstone.project.orchestrator.pay.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPayHistoryDetail is a Querydsl query type for PayHistoryDetail
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPayHistoryDetail extends EntityPathBase<PayHistoryDetail> {

    private static final long serialVersionUID = -1225177823L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPayHistoryDetail payHistoryDetail = new QPayHistoryDetail("payHistoryDetail");

    public final eureca.capstone.project.orchestrator.common.entity.QBaseEntity _super = new eureca.capstone.project.orchestrator.common.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final eureca.capstone.project.orchestrator.transaction_feed.entity.QDataTransactionHistory dataTransactionHistory;

    public final QPayHistory payHistory;

    public final NumberPath<Long> payHistoryDetailId = createNumber("payHistoryDetailId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QPayHistoryDetail(String variable) {
        this(PayHistoryDetail.class, forVariable(variable), INITS);
    }

    public QPayHistoryDetail(Path<? extends PayHistoryDetail> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPayHistoryDetail(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPayHistoryDetail(PathMetadata metadata, PathInits inits) {
        this(PayHistoryDetail.class, metadata, inits);
    }

    public QPayHistoryDetail(Class<? extends PayHistoryDetail> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.dataTransactionHistory = inits.isInitialized("dataTransactionHistory") ? new eureca.capstone.project.orchestrator.transaction_feed.entity.QDataTransactionHistory(forProperty("dataTransactionHistory"), inits.get("dataTransactionHistory")) : null;
        this.payHistory = inits.isInitialized("payHistory") ? new QPayHistory(forProperty("payHistory"), inits.get("payHistory")) : null;
    }

}

