package eureca.capstone.project.orchestrator.transaction_feed.service;

import eureca.capstone.project.orchestrator.transaction_feed.dto.request.CreateFeedRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.UpdateFeedRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.CreateFeedResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.GetFeedDetailResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.UpdateFeedResponseDto;

public interface TransactionFeedService {
    CreateFeedResponseDto createFeed(String email, CreateFeedRequestDto feedRequestDto);
    UpdateFeedResponseDto updateFeed(String email, UpdateFeedRequestDto updateFeedRequestDto);
    GetFeedDetailResponseDto getFeedDetail(Long transactionFeedId);
}
