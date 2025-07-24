package eureca.capstone.project.orchestrator.pay.dto;

import eureca.capstone.project.orchestrator.pay.entity.Bank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BankDto {
    private Long bankId;
    private String bankName;

    public static BankDto fromEntity(Bank bank) {
        return BankDto.builder()
                .bankId(bank.getBankId())
                .bankName(bank.getBankName())
                .build();
    }
}
