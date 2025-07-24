package eureca.capstone.project.orchestrator.pay.repository;

import eureca.capstone.project.orchestrator.pay.entity.EventCoupon;
import eureca.capstone.project.orchestrator.pay.entity.UserEventCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventCouponRepository extends JpaRepository<EventCoupon, Long> {

}
