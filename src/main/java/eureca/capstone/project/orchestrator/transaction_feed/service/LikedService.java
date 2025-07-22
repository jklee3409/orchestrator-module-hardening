package eureca.capstone.project.orchestrator.transaction_feed.service;

import eureca.capstone.project.orchestrator.transaction_feed.dto.request.AddWishFeedRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.RemoveWishFeedsRequestDto;

public interface LikedService {
    void addWishFeed(String email, AddWishFeedRequestDto addWishFeedRequestDto);
    void removeWishFeed(String email, RemoveWishFeedsRequestDto requestDto);
}
