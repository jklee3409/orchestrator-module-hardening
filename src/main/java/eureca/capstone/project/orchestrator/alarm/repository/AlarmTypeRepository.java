package eureca.capstone.project.orchestrator.alarm.repository;

import eureca.capstone.project.orchestrator.alarm.entity.AlarmType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmTypeRepository extends JpaRepository<AlarmType, Long> {
}
