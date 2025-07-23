package eureca.capstone.project.orchestrator.transaction_feed.dto.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionHistoryType {
    ALL,
    PURCHASE,
    SALE;
}
