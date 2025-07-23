package eureca.capstone.project.orchestrator.transaction_feed.service;

import eureca.capstone.project.orchestrator.transaction_feed.dto.UserDataCouponDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.UseDataCouponResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DataCouponService {
    void issueDataCoupon(User buyer, TransactionFeed purchaseFeed);
    Page<UserDataCouponDto> getUserDataCouponList(String email, Pageable pageable);
    UseDataCouponResponseDto useDataCoupon(String email, Long userDataCouponId);
}
