package eureca.capstone.project.orchestrator.transaction_feed.repository;

import eureca.capstone.project.orchestrator.common.entity.TelecomCompany;
import eureca.capstone.project.orchestrator.transaction_feed.entity.DataCoupon;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataCouponRepository extends JpaRepository<DataCoupon, Long> {
    Optional<DataCoupon> findByDataAmountAndTelecomCompany(Long dataAmount, TelecomCompany telecomCompany);
}
