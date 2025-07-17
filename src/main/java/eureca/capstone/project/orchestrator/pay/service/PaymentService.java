package eureca.capstone.project.orchestrator.pay.service;

import eureca.capstone.project.orchestrator.pay.dto.request.CouponCalculationRequestDto;
import eureca.capstone.project.orchestrator.pay.dto.request.PaymentPrepareRequestDto;
import eureca.capstone.project.orchestrator.pay.dto.response.CouponCalculationResponseDto;
import eureca.capstone.project.orchestrator.pay.dto.response.PaymentPrepareResponseDto;

public interface PaymentService {
    CouponCalculationResponseDto calculateDiscount(String email, CouponCalculationRequestDto couponCalculationRequestDto);
    PaymentPrepareResponseDto preparePayment(String email, PaymentPrepareRequestDto paymentPrepareRequestDto );
}
