package eureca.capstone.project.orchestrator.transaction_feed.service;

import eureca.capstone.project.orchestrator.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.GetFeedSummaryResponseDto;
import java.util.List;

public interface RecommendService {
    List<GetFeedSummaryResponseDto> recommendFeed(CustomUserDetailsDto customUserDetailsDto);
    List<GetFeedSummaryResponseDto> recommendRelateFeeds(Long transactionFeedId);
}
