package eureca.capstone.project.orchestrator.transaction_feed.controller;


import eureca.capstone.project.orchestrator.common.dto.base.BaseResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.CreateFeedRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.CreateFeedResponse;
import eureca.capstone.project.orchestrator.transaction_feed.repository.TransactionFeedRepository;
import eureca.capstone.project.orchestrator.transaction_feed.service.TransactionFeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/transaction-feed")
@RestController
@RequiredArgsConstructor
public class TransactionFeedController {

    private final TransactionFeedService transactionFeedService;

    @PostMapping
    public BaseResponseDto<CreateFeedResponse> createFeed(@RequestBody CreateFeedRequestDto createFeedRequestDto) {
        CreateFeedResponse createFeedResponse = transactionFeedService.createFeed(createFeedRequestDto);
        return BaseResponseDto.success(createFeedResponse);
    }


}
