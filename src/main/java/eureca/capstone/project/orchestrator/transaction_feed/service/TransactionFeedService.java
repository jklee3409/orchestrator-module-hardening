package eureca.capstone.project.orchestrator.transaction_feed.service;

import eureca.capstone.project.orchestrator.transaction_feed.dto.request.CreateFeedRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.CreateFeedResponseDto;

public interface TransactionFeedService {
    CreateFeedResponseDto createFeed(String email, CreateFeedRequestDto feedRequestDto);
}
