package eureca.capstone.project.orchestrator.alarm.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAlarmType is a Querydsl query type for AlarmType
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAlarmType extends EntityPathBase<AlarmType> {

    private static final long serialVersionUID = -308175874L;

    public static final QAlarmType alarmType = new QAlarmType("alarmType");

    public final eureca.capstone.project.orchestrator.common.entity.QBaseEntity _super = new eureca.capstone.project.orchestrator.common.entity.QBaseEntity(this);

    public final NumberPath<Long> alarmTypeId = createNumber("alarmTypeId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath type = createString("type");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QAlarmType(String variable) {
        super(AlarmType.class, forVariable(variable));
    }

    public QAlarmType(Path<? extends AlarmType> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAlarmType(PathMetadata metadata) {
        super(AlarmType.class, metadata);
    }

}

