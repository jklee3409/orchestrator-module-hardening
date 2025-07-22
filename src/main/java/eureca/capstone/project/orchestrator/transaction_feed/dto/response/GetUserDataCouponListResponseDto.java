package eureca.capstone.project.orchestrator.transaction_feed.dto.response;

import eureca.capstone.project.orchestrator.transaction_feed.dto.UserDataCouponDto;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetUserDataCouponListResponseDto {
    List<UserDataCouponDto> dataCoupons;
}
