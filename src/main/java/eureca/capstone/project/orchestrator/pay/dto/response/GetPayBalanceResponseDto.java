package eureca.capstone.project.orchestrator.pay.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetPayBalanceResponseDto {
    private final Long balance;

    public static GetPayBalanceResponseDto from(Long balance) {
        return GetPayBalanceResponseDto.builder()
                .balance(balance)
                .build();
    }
}
