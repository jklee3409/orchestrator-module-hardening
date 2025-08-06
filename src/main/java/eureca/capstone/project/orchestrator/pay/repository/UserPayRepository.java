package eureca.capstone.project.orchestrator.pay.repository;

import eureca.capstone.project.orchestrator.pay.entity.UserPay;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPayRepository extends JpaRepository<UserPay, Long> {
    Optional<UserPay> findByUserId(Long userId);
}
