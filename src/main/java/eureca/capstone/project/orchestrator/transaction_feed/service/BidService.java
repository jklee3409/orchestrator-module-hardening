package eureca.capstone.project.orchestrator.transaction_feed.service;

import eureca.capstone.project.orchestrator.transaction_feed.dto.request.PlaceBidRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.GetBidHistoryResponseDto;

public interface BidService {
    void placeBid(String email, PlaceBidRequestDto placeBidRequestDto);
    GetBidHistoryResponseDto getBidHistory(Long transactionFeedId);
}
