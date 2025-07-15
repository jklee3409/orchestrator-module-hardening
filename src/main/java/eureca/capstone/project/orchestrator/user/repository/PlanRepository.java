package eureca.capstone.project.orchestrator.user.repository;

import eureca.capstone.project.orchestrator.common.entity.TelecomCompany;
import eureca.capstone.project.orchestrator.user.entity.Plan;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlanRepository extends JpaRepository<Plan, Long> {

    @Query("SELECT p FROM Plan p WHERE p.telecomCompany = :telecomCompany ORDER BY FUNCTION('RAND')")
    Optional<Plan> findRandomPlanByTelecomCompany(@Param("telecomCompany") TelecomCompany telecomCompany);
}
