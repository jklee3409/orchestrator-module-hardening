package eureca.capstone.project.orchestrator.pay.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserPay is a Querydsl query type for UserPay
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserPay extends EntityPathBase<UserPay> {

    private static final long serialVersionUID = -1665083015L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserPay userPay = new QUserPay("userPay");

    public final eureca.capstone.project.orchestrator.common.entity.QBaseEntity _super = new eureca.capstone.project.orchestrator.common.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> pay = createNumber("pay", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final eureca.capstone.project.orchestrator.user.entity.QUser user;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QUserPay(String variable) {
        this(UserPay.class, forVariable(variable), INITS);
    }

    public QUserPay(Path<? extends UserPay> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserPay(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserPay(PathMetadata metadata, PathInits inits) {
        this(UserPay.class, metadata, inits);
    }

    public QUserPay(Class<? extends UserPay> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new eureca.capstone.project.orchestrator.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

