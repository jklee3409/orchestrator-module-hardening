package eureca.capstone.project.orchestrator.user.repository;

import eureca.capstone.project.orchestrator.common.entity.TelecomCompany;
import eureca.capstone.project.orchestrator.user.entity.Plan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface PlanRepository extends JpaRepository<Plan, Long> {

    @Query("SELECT p FROM Plan p WHERE p.telecomCompany = :telecomCompany ORDER BY FUNCTION('RAND')")
    Page<Plan> findRandomPlanByTelecomCompany(@Param("telecomCompany") TelecomCompany telecomCompany, Pageable pageable);
}
