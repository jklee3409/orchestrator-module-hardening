package eureca.capstone.project.orchestrator.pay.service;

import eureca.capstone.project.orchestrator.pay.dto.request.ExchangeRequestDto;

public interface ExchangeService {
    void exchangePay(String email, ExchangeRequestDto requestDto);
}
