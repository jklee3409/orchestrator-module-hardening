package eureca.capstone.project.orchestrator.pay.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QExchangeHistory is a Querydsl query type for ExchangeHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QExchangeHistory extends EntityPathBase<ExchangeHistory> {

    private static final long serialVersionUID = 155326861L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QExchangeHistory exchangeHistory = new QExchangeHistory("exchangeHistory");

    public final eureca.capstone.project.orchestrator.common.entity.QBaseEntity _super = new eureca.capstone.project.orchestrator.common.entity.QBaseEntity(this);

    public final NumberPath<Long> amount = createNumber("amount", Long.class);

    public final QBank bank;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath exchangeAccount = createString("exchangeAccount");

    public final NumberPath<Long> exchangeHistoryId = createNumber("exchangeHistoryId", Long.class);

    public final NumberPath<Long> fee = createNumber("fee", Long.class);

    public final NumberPath<Long> finalAmount = createNumber("finalAmount", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final eureca.capstone.project.orchestrator.user.entity.QUser user;

    public QExchangeHistory(String variable) {
        this(ExchangeHistory.class, forVariable(variable), INITS);
    }

    public QExchangeHistory(Path<? extends ExchangeHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QExchangeHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QExchangeHistory(PathMetadata metadata, PathInits inits) {
        this(ExchangeHistory.class, metadata, inits);
    }

    public QExchangeHistory(Class<? extends ExchangeHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.bank = inits.isInitialized("bank") ? new QBank(forProperty("bank")) : null;
        this.user = inits.isInitialized("user") ? new eureca.capstone.project.orchestrator.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

