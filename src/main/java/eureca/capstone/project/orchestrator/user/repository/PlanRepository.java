package eureca.capstone.project.orchestrator.user.repository;

import eureca.capstone.project.orchestrator.common.entity.TelecomCompany;
import eureca.capstone.project.orchestrator.user.entity.Plan;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlanRepository extends JpaRepository<Plan, Long> {

    @Query(value = "SELECT * FROM plan WHERE telecom_company = :telecomCompany ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<Plan> findRandomPlanByTelecomCompany(@Param("telecomCompany") TelecomCompany telecomCompany);
}
