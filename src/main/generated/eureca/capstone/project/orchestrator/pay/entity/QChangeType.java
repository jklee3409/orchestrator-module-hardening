package eureca.capstone.project.orchestrator.pay.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QChangeType is a Querydsl query type for ChangeType
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChangeType extends EntityPathBase<ChangeType> {

    private static final long serialVersionUID = -1236531986L;

    public static final QChangeType changeType = new QChangeType("changeType");

    public final eureca.capstone.project.orchestrator.common.entity.QBaseEntity _super = new eureca.capstone.project.orchestrator.common.entity.QBaseEntity(this);

    public final NumberPath<Long> changeTypeId = createNumber("changeTypeId", Long.class);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath type = createString("type");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QChangeType(String variable) {
        super(ChangeType.class, forVariable(variable));
    }

    public QChangeType(Path<? extends ChangeType> path) {
        super(path.getType(), path.getMetadata());
    }

    public QChangeType(PathMetadata metadata) {
        super(ChangeType.class, metadata);
    }

}

