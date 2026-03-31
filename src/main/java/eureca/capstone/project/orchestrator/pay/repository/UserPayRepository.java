package eureca.capstone.project.orchestrator.pay.repository;

import eureca.capstone.project.orchestrator.pay.entity.UserPay;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserPayRepository extends JpaRepository<UserPay, Long> {
    Optional<UserPay> findByUserId(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                    update UserPay up
                    set up.pay = up.pay - :amount
                    where up.userId = :userId
                    and up.pay >= :amount
            """)
    int decreasePayIfEnough(@Param("userId") Long userId, @Param("amount") Long amount);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                    update UserPay up
                    set up.pay = up.pay + :amount
                    where up.userId = :userId
            """)
    int increasePay(@Param("userId") Long userId, @Param("amount") Long amount);
}
