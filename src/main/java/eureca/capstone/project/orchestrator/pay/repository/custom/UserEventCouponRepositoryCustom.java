package eureca.capstone.project.orchestrator.pay.repository.custom;

import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.pay.entity.UserEventCoupon;
import eureca.capstone.project.orchestrator.user.entity.User;
import java.util.List;

public interface UserEventCouponRepositoryCustom {
    List<UserEventCoupon> findAvailableCouponsByUserAndStatus(User user, Status status);
}
