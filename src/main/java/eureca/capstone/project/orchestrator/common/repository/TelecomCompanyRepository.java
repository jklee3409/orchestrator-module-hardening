package eureca.capstone.project.orchestrator.common.repository;

import eureca.capstone.project.orchestrator.common.entity.TelecomCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TelecomCompanyRepository extends JpaRepository<TelecomCompany, Long> {

    @Query(value = "SELECT * FROM telecom_company ORDER BY RAND() LIMIT 1", nativeQuery = true)
    TelecomCompany findRandomTelecomCompany();
}
