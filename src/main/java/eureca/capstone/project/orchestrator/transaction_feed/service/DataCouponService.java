package eureca.capstone.project.orchestrator.transaction_feed.service;

import eureca.capstone.project.orchestrator.transaction_feed.dto.response.GetUserDataCouponListResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.user.entity.User;

public interface DataCouponService {
    void issueDataCoupon(User buyer, TransactionFeed purchaseFeed);
    GetUserDataCouponListResponseDto getUserDataCouponList(String email);
}
