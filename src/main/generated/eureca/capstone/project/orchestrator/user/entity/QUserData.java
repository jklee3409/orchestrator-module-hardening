package eureca.capstone.project.orchestrator.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUserData is a Querydsl query type for UserData
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserData extends EntityPathBase<UserData> {

    private static final long serialVersionUID = 889398590L;

    public static final QUserData userData = new QUserData("userData");

    public final eureca.capstone.project.orchestrator.common.entity.QBaseEntity _super = new eureca.capstone.project.orchestrator.common.entity.QBaseEntity(this);

    public final NumberPath<Integer> buyerDataMb = createNumber("buyerDataMb", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> planId = createNumber("planId", Long.class);

    public final NumberPath<Integer> resetDataAt = createNumber("resetDataAt", Integer.class);

    public final NumberPath<Integer> sellableDataMb = createNumber("sellableDataMb", Integer.class);

    public final NumberPath<Integer> totalDataMb = createNumber("totalDataMb", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> userDataId = createNumber("userDataId", Long.class);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QUserData(String variable) {
        super(UserData.class, forVariable(variable));
    }

    public QUserData(Path<? extends UserData> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserData(PathMetadata metadata) {
        super(UserData.class, metadata);
    }

}

