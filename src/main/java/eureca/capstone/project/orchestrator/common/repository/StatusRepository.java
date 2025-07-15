package eureca.capstone.project.orchestrator.common.repository;

import eureca.capstone.project.orchestrator.common.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusRepository extends JpaRepository<Status, Long> {
}
