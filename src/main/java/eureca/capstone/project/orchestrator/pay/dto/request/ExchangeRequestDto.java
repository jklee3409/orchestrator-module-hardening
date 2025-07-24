package eureca.capstone.project.orchestrator.pay.dto.request;

import lombok.Data;

@Data
public class ExchangeRequestDto {
    private Long bankId;
    private String exchangeAccount;
    private Long amount;
}
