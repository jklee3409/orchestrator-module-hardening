package eureca.capstone.project.orchestrator.market_statistics.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMarketStatistics is a Querydsl query type for MarketStatistics
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMarketStatistics extends EntityPathBase<MarketStatistics> {

    private static final long serialVersionUID = 1690093953L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMarketStatistics marketStatistics = new QMarketStatistics("marketStatistics");

    public final eureca.capstone.project.orchestrator.common.entity.QBaseEntity _super = new eureca.capstone.project.orchestrator.common.entity.QBaseEntity(this);

    public final NumberPath<Integer> averagePrice = createNumber("averagePrice", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> staticsTime = createDateTime("staticsTime", java.time.LocalDateTime.class);

    public final NumberPath<Long> statisticsId = createNumber("statisticsId", Long.class);

    public final eureca.capstone.project.orchestrator.common.entity.QTelecomCompany telecomCompany;

    public final NumberPath<Integer> transactionAmount = createNumber("transactionAmount", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QMarketStatistics(String variable) {
        this(MarketStatistics.class, forVariable(variable), INITS);
    }

    public QMarketStatistics(Path<? extends MarketStatistics> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMarketStatistics(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMarketStatistics(PathMetadata metadata, PathInits inits) {
        this(MarketStatistics.class, metadata, inits);
    }

    public QMarketStatistics(Class<? extends MarketStatistics> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.telecomCompany = inits.isInitialized("telecomCompany") ? new eureca.capstone.project.orchestrator.common.entity.QTelecomCompany(forProperty("telecomCompany")) : null;
    }

}

