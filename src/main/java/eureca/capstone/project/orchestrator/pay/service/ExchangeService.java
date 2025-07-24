package eureca.capstone.project.orchestrator.pay.service;

import eureca.capstone.project.orchestrator.pay.dto.BankDto;
import eureca.capstone.project.orchestrator.pay.dto.request.ExchangeRequestDto;
import java.util.List;

public interface ExchangeService {
    void exchangePay(String email, ExchangeRequestDto requestDto);
    List<BankDto> getBankList();
}
