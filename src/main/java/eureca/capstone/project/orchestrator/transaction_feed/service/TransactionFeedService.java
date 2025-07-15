package eureca.capstone.project.orchestrator.transaction_feed.service;

import eureca.capstone.project.orchestrator.transaction_feed.dto.request.CreateFeedRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.CreateFeedResponse;

public interface TransactionFeedService {
    CreateFeedResponse createFeed(CreateFeedRequestDto feedRequestDto);
}
