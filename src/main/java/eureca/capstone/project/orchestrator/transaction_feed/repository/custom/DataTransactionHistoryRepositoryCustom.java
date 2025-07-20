package eureca.capstone.project.orchestrator.transaction_feed.repository.custom;

import eureca.capstone.project.orchestrator.transaction_feed.dto.UserTransactionAverageDto;
import eureca.capstone.project.orchestrator.user.entity.User;
import java.util.Optional;

public interface DataTransactionHistoryRepositoryCustom {
    Optional<UserTransactionAverageDto> findAverageByUser(User user);
}
