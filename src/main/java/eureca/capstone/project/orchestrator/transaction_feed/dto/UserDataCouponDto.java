package eureca.capstone.project.orchestrator.transaction_feed.dto;

import eureca.capstone.project.orchestrator.common.dto.StatusDto;
import eureca.capstone.project.orchestrator.common.dto.TelecomCompanyDto;
import eureca.capstone.project.orchestrator.transaction_feed.entity.UserDataCoupon;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDataCouponDto {
    private Long userDataCouponId;
    private String couponNumber;
    private Long dataAmount;
    private TelecomCompanyDto telecomCompany;
    private StatusDto status;
    private LocalDateTime expiresAt;

    public static UserDataCouponDto fromEntity(UserDataCoupon userDataCoupon) {
        return UserDataCouponDto.builder()
                .userDataCouponId(userDataCoupon.getUserDataCouponId())
                .couponNumber(userDataCoupon.getDataCoupon().getCouponNumber())
                .dataAmount(userDataCoupon.getDataCoupon().getDataAmount())
                .telecomCompany(TelecomCompanyDto.fromEntity(userDataCoupon.getDataCoupon().getTelecomCompany()))
                .status(StatusDto.fromEntity(userDataCoupon.getStatus()))
                .expiresAt(userDataCoupon.getExpiresAt())
                .build();
    }
}
