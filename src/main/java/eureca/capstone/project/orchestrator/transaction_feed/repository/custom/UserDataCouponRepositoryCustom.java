package eureca.capstone.project.orchestrator.transaction_feed.repository.custom;

import eureca.capstone.project.orchestrator.transaction_feed.entity.UserDataCoupon;
import eureca.capstone.project.orchestrator.user.entity.User;
import java.util.List;

public interface UserDataCouponRepositoryCustom {
    List<UserDataCoupon> findDetailsByUser(User user);
}
