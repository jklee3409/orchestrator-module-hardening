package eureca.capstone.project.orchestrator.pay.repository;

import eureca.capstone.project.orchestrator.pay.entity.Bank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankRepository extends JpaRepository<Bank, Long> {
}
