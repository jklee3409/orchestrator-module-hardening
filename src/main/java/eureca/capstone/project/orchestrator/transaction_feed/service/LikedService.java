package eureca.capstone.project.orchestrator.transaction_feed.service;

import eureca.capstone.project.orchestrator.transaction_feed.dto.enums.WishListFilter;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.AddWishFeedRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.RemoveWishFeedsRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.GetFeedSummaryResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LikedService {
    Page<GetFeedSummaryResponseDto> getWishList(String email, WishListFilter filter, Pageable pageable);
    void addWishFeed(String email, AddWishFeedRequestDto addWishFeedRequestDto);
    void removeWishFeed(String email, RemoveWishFeedsRequestDto requestDto);
}
