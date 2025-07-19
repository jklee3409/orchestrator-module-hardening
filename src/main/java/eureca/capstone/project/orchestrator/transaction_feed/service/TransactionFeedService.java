package eureca.capstone.project.orchestrator.transaction_feed.service;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.AddWishFeedRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.CreateFeedRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.FeedSearchRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.UpdateFeedRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.CreateFeedResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.GetFeedDetailResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.GetFeedSummaryResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.UpdateFeedResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionFeedService {
    CreateFeedResponseDto createFeed(String email, CreateFeedRequestDto feedRequestDto);
    UpdateFeedResponseDto updateFeed(String email, UpdateFeedRequestDto updateFeedRequestDto);
    GetFeedDetailResponseDto getFeedDetail(Long transactionFeedId, CustomUserDetailsDto customUserDetailsDto);
    void deleteFeed(String email, Long transactionFeedId);
    Page<GetFeedSummaryResponseDto> searchFeeds(FeedSearchRequestDto feedSearchRequestDto, Pageable pageable, CustomUserDetailsDto customUserDetailsDto);
    void addWishFeed(String email, AddWishFeedRequestDto addWishFeedRequestDto);
    void removeWishFeed(String email, Long transactionFeedId);
}
