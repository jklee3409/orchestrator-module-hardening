package eureca.capstone.project.orchestrator.transaction_feed.repository;

import eureca.capstone.project.orchestrator.transaction_feed.entity.UserDataCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDataCouponRepository extends JpaRepository<UserDataCoupon, Long> {
}
