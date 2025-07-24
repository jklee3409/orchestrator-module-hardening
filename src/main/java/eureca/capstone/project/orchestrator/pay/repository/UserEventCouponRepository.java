package eureca.capstone.project.orchestrator.pay.repository;

import eureca.capstone.project.orchestrator.pay.entity.EventCoupon;
import eureca.capstone.project.orchestrator.pay.entity.UserEventCoupon;
import eureca.capstone.project.orchestrator.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserEventCouponRepository extends JpaRepository<UserEventCoupon, Long> {
    boolean existsByUserAndEventCoupon(User user, EventCoupon eventCoupon);
}
