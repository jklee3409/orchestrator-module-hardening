package eureca.capstone.project.orchestrator.pay.dto.response;

import eureca.capstone.project.orchestrator.pay.dto.UserEventCouponDto;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetUserEventCouponListResponseDto {
    List<UserEventCouponDto> coupons;
}
