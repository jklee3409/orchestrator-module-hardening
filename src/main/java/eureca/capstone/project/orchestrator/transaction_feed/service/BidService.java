package eureca.capstone.project.orchestrator.transaction_feed.service;

import eureca.capstone.project.orchestrator.transaction_feed.dto.request.PlaceBidRequestDto;

public interface BidService {
    void placeBid(String email, PlaceBidRequestDto placeBidRequestDto);
}
