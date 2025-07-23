package eureca.capstone.project.orchestrator.transaction_feed.repository.custom;

import eureca.capstone.project.orchestrator.transaction_feed.entity.UserDataCoupon;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.entity.UserData;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserDataCouponRepositoryCustom {
    Page<UserDataCoupon> findDetailsByUser(User user, Pageable pageable);
    Optional<UserDataCoupon> findDetailsById(Long userDataCouponId);
}
