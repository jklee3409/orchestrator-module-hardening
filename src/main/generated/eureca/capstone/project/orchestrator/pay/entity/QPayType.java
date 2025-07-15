package eureca.capstone.project.orchestrator.pay.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPayType is a Querydsl query type for PayType
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPayType extends EntityPathBase<PayType> {

    private static final long serialVersionUID = 1989624990L;

    public static final QPayType payType = new QPayType("payType");

    public final eureca.capstone.project.orchestrator.common.entity.QBaseEntity _super = new eureca.capstone.project.orchestrator.common.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath name = createString("name");

    public final NumberPath<Long> payTypeId = createNumber("payTypeId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QPayType(String variable) {
        super(PayType.class, forVariable(variable));
    }

    public QPayType(Path<? extends PayType> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPayType(PathMetadata metadata) {
        super(PayType.class, metadata);
    }

}

