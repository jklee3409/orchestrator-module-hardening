package eureca.capstone.project.orchestrator.pay.service;

import eureca.capstone.project.orchestrator.pay.dto.request.CouponCalculationRequestDto;
import eureca.capstone.project.orchestrator.pay.dto.response.CouponCalculationResponseDto;

public interface PaymentService {
    CouponCalculationResponseDto calculateDiscount(String email, CouponCalculationRequestDto couponCalculationRequestDto);
}
