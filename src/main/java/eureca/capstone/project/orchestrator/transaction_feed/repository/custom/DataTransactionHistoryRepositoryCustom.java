package eureca.capstone.project.orchestrator.transaction_feed.repository.custom;

import eureca.capstone.project.orchestrator.transaction_feed.dto.UserTransactionAverageDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.enums.TransactionHistoryType;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.GetTransactionHistoryResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.entity.DataTransactionHistory;
import eureca.capstone.project.orchestrator.user.entity.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DataTransactionHistoryRepositoryCustom {
    Optional<UserTransactionAverageDto> findAverageByUser(User user);
    Page<DataTransactionHistory> findTransactionHistoryByUserId(Long userId, TransactionHistoryType type, Pageable pageable);
}
