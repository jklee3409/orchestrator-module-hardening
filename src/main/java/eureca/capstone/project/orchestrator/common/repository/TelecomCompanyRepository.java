package eureca.capstone.project.orchestrator.common.repository;

import eureca.capstone.project.orchestrator.common.entiry.TelecomCompany;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelecomCompanyRepository extends JpaRepository<TelecomCompany, Long> {
}
