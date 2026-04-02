package eureca.capstone.project.orchestrator.transaction_feed.service.impl;

import eureca.capstone.project.orchestrator.common.constant.RedisConstant;
import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.common.exception.code.ErrorCode;
import eureca.capstone.project.orchestrator.common.exception.custom.BidException;
import eureca.capstone.project.orchestrator.common.exception.custom.InternalServerException;
import eureca.capstone.project.orchestrator.common.exception.custom.TransactionFeedNotFoundException;
import eureca.capstone.project.orchestrator.common.exception.custom.UserNotFoundException;
import eureca.capstone.project.orchestrator.common.util.SalesTypeManager;
import eureca.capstone.project.orchestrator.common.util.StatusManager;
import eureca.capstone.project.orchestrator.pay.entity.UserPay;
import eureca.capstone.project.orchestrator.pay.repository.UserPayRepository;
import eureca.capstone.project.orchestrator.pay.service.UserPayService;
import eureca.capstone.project.orchestrator.transaction_feed.dto.BidDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.PlaceBidRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.GetBidHistoryResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.entity.Bids;
import eureca.capstone.project.orchestrator.transaction_feed.entity.SalesType;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.transaction_feed.event.BidSucceededEvent;
import eureca.capstone.project.orchestrator.transaction_feed.repository.BidsRepository;
import eureca.capstone.project.orchestrator.transaction_feed.repository.TransactionFeedRepository;
import eureca.capstone.project.orchestrator.transaction_feed.service.BidService;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidServiceImpl implements BidService {

    private final UserRepository userRepository;
    private final TransactionFeedRepository transactionFeedRepository;
    private final BidsRepository bidsRepository;
    private final UserPayService userPayService;
    private final UserPayRepository userPayRepository;
    private final StringRedisTemplate stringRedisTemplate;
    @Qualifier("bidScript") private final RedisScript<List> bidScript;
    @Qualifier("bidRollbackScript") private final RedisScript<Long> bidRollbackScript;
    private final StatusManager statusManager;
    private final SalesTypeManager salesTypeManager;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void placeBid(String email, PlaceBidRequestDto request) {
        User bidder = findUserByEmail(email);
        TransactionFeed feed = findTransactionFeedById(request.getTransactionFeedId());
        log.info("[placeBid] 사용자 ID: {}, 판매글 ID: {}, 입찰금액: {}",
                bidder.getUserId(), feed.getTransactionFeedId(), request.getBidAmount());

        validateBidPrecondition(bidder, feed, request.getBidAmount());
        log.info("[placeBid] 입찰 사전 검증 통과");

        LocalDateTime bidTimeStamp = LocalDateTime.now();
        BidRedisKeys redisKeys = BidRedisKeys.fromFeedId(feed.getTransactionFeedId());

        List result = stringRedisTemplate.execute(
                bidScript,
                redisKeys.asList(),
                request.getBidAmount().toString(),
                bidder.getUserId().toString(),
                feed.getSalesPrice().toString()
        );
        log.info("[placeBid] 레디스 스크립트 실행 결과: {}", result);

        handleBidResult(result, feed.getTransactionFeedId(), bidder, request.getBidAmount(), bidTimeStamp, redisKeys);
    }

    @Override
    @Transactional(readOnly = true)
    public GetBidHistoryResponseDto getBidHistory(Long transactionFeedId) {
        log.info("[getBidHistory] 판매글 ID {} 입찰 내역 조회 시작", transactionFeedId);

        TransactionFeed feed = findTransactionFeedById(transactionFeedId);
        log.info("[getBidHistory] 판매글 조회 완료. ID: {}", transactionFeedId);

        SalesType bidSalesType = salesTypeManager.getBidSaleType();
        if (!feed.getSalesType().equals(bidSalesType)) {
            log.error("[getBidHistory] 해당 판매글(ID:{})은 입찰 판매글이 아닙니다.", transactionFeedId);
            throw new BidException(ErrorCode.FEED_NOT_AUCTION);
        }

        List<Bids> bids = bidsRepository.findBidsWithUserByTransactionFeed(feed);
        List<BidDto> bidDtos = bids.stream()
                .map(BidDto::fromEntity)
                .collect(Collectors.toList());

        return GetBidHistoryResponseDto.builder()
                .bids(bidDtos)
                .build();
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }

    private TransactionFeed findTransactionFeedById(Long transactionFeedId) {
        return transactionFeedRepository.findById(transactionFeedId)
                .orElseThrow(TransactionFeedNotFoundException::new);
    }

    private TransactionFeed findTransactionFeedByIdForUpdate(Long transactionFeedId) {
        return transactionFeedRepository.findByIdForUpdate(transactionFeedId)
                .orElseThrow(TransactionFeedNotFoundException::new);
    }

    private void validateBidPrecondition(User bidder, TransactionFeed feed, Long bidAmount) {
        log.info("[validateBidPrecondition] 입찰 사전 검증 시작 - 사용자: {}, 판매글 ID: {}, 입찰가: {}",
                bidder.getUserId(), feed.getTransactionFeedId(), bidAmount);

        Status salesStatus = statusManager.getStatus("FEED", "ON_SALE");
        SalesType bidSalesType = salesTypeManager.getBidSaleType();
        UserPay userPay = userPayRepository.findByUserId(bidder.getUserId()).orElseThrow(UserNotFoundException::new);
        Long bidderPay = userPay.getPay();

        if (feed.getUser().getUserId().equals(bidder.getUserId())) {
            throw new BidException(ErrorCode.SELLER_CANNOT_BID);
        }
        if (!feed.getTelecomCompany().getTelecomCompanyId().equals(bidder.getTelecomCompany().getTelecomCompanyId())) {
            throw new BidException(ErrorCode.INVALID_TELECOM_COMPANY);
        }
        if (!feed.getStatus().equals(salesStatus)) {
            throw new BidException(ErrorCode.AUCTION_NOT_ON_SALE);
        }
        if (!feed.getSalesType().equals(bidSalesType)) {
            throw new BidException(ErrorCode.FEED_NOT_AUCTION);
        }
        if (feed.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BidException(ErrorCode.AUCTION_EXPIRED);
        }
        if (bidderPay < bidAmount) {
            throw new BidException(ErrorCode.USER_PAY_LACK);
        }
        if (bidAmount % 100 != 0) {
            throw new BidException(ErrorCode.BID_AMOUNT_100_DIVISIBLE);
        }
    }

    private void handleBidResult(
            List<Object> result,
            Long transactionFeedId,
            User newBidder,
            Long bidAmount,
            LocalDateTime bidTimeStamp,
            BidRedisKeys redisKeys
    ) {
        if (result == null || result.isEmpty()) {
            throw new InternalServerException(ErrorCode.LUA_SCRIPT_ERROR);
        }

        String status = String.valueOf(result.get(0));

        switch (Objects.requireNonNull(status)) {
            case "SUCCESS" -> {
                BidRedisSuccessResult successResult = BidRedisSuccessResult.from(result);

                try {
                    finalizeBidAgainstCommittedState(
                            transactionFeedId,
                            newBidder,
                            bidAmount,
                            bidTimeStamp,
                            successResult
                    );
                } catch (Exception e) {
                    log.error("[handleBidResult] 입찰 확정 단계에서 예외 발생. 버전 기반 보상 롤백을 시도합니다.", e);
                    rollbackRedisBidState(redisKeys, successResult);
                    throw e;
                }
            }
            case "BID_TOO_LOW" -> throw new BidException(ErrorCode.BID_AMOUNT_TOO_LOW);
            case "SAME_BIDDER" -> throw new BidException(ErrorCode.CANNOT_BID_ON_OWN_HIGHEST);
            default -> throw new InternalServerException(ErrorCode.LUA_SCRIPT_ERROR);
        }
    }

    private void finalizeBidAgainstCommittedState(
            Long transactionFeedId,
            User newBidder,
            Long bidAmount,
            LocalDateTime bidTimeStamp,
            BidRedisSuccessResult successResult
    ) {
        TransactionFeed lockedFeed = findTransactionFeedByIdForUpdate(transactionFeedId);
        validateBidFinalizationState(lockedFeed);

        Optional<Bids> currentHighestBid = bidsRepository
                .findTopByTransactionFeedOrderByBidAmountDescBidTimeDesc(lockedFeed);

        logRedisDbGapIfNeeded(successResult, currentHighestBid);
        validateAgainstCommittedHighest(currentHighestBid, newBidder, bidAmount);

        currentHighestBid.ifPresent(this::refundCommittedHighestBid);

        userPayService.usePay(newBidder, bidAmount);

        saveBidHistory(lockedFeed, newBidder, bidAmount, bidTimeStamp);

        applicationEventPublisher.publishEvent(BidSucceededEvent.of(lockedFeed, newBidder, bidAmount));
        log.info("[finalizeBidAgainstCommittedState] AFTER_COMMIT 후속 처리를 위한 이벤트 발행 완료. 판매글 ID: {}, 사용자 ID: {}, 입찰가: {}",
                lockedFeed.getTransactionFeedId(), newBidder.getUserId(), bidAmount);
    }

    private void validateBidFinalizationState(TransactionFeed feed) {
        Status salesStatus = statusManager.getStatus("FEED", "ON_SALE");
        SalesType bidSalesType = salesTypeManager.getBidSaleType();

        if (!feed.getStatus().equals(salesStatus)) {
            throw new BidException(ErrorCode.AUCTION_NOT_ON_SALE);
        }
        if (!feed.getSalesType().equals(bidSalesType)) {
            throw new BidException(ErrorCode.FEED_NOT_AUCTION);
        }
        if (feed.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BidException(ErrorCode.AUCTION_EXPIRED);
        }
    }

    private void validateAgainstCommittedHighest(Optional<Bids> currentHighestBid, User newBidder, Long bidAmount) {
        if (currentHighestBid.isEmpty()) {
            return;
        }

        Bids committedHighestBid = currentHighestBid.get();
        Long committedHighestBidderId = committedHighestBid.getUser().getUserId();
        Long committedHighestAmount = committedHighestBid.getBidAmount();

        if (committedHighestBidderId.equals(newBidder.getUserId())) {
            throw new BidException(ErrorCode.CANNOT_BID_ON_OWN_HIGHEST);
        }

        if (committedHighestAmount >= bidAmount) {
            throw new BidException(ErrorCode.BID_AMOUNT_TOO_LOW);
        }
    }

    private void refundCommittedHighestBid(Bids committedHighestBid) {
        userPayService.refundPay(committedHighestBid.getUser(), committedHighestBid.getBidAmount());
    }

    private void logRedisDbGapIfNeeded(BidRedisSuccessResult successResult, Optional<Bids> currentHighestBid) {
        if (currentHighestBid.isEmpty()) {
            return;
        }

        Bids committedHighestBid = currentHighestBid.get();
        String committedBidderId = String.valueOf(committedHighestBid.getUser().getUserId());
        String committedAmount = String.valueOf(committedHighestBid.getBidAmount());

        if (!successResult.hasPreviousBid()) {
            log.warn("[logRedisDbGapIfNeeded] Redis는 이전 최고 입찰이 없다고 보지만 DB에는 존재합니다. dbBidder={}, dbAmount={}",
                    committedBidderId, committedAmount);
            return;
        }

        boolean sameBidder = committedBidderId.equals(successResult.prevBidderId());
        boolean sameAmount = committedAmount.equals(successResult.prevBidAmount());

        if (!sameBidder || !sameAmount) {
            log.warn("[logRedisDbGapIfNeeded] Redis 이전 상태와 DB 커밋 상태가 다릅니다. redisBidder={}, redisAmount={}, dbBidder={}, dbAmount={}",
                    successResult.prevBidderId(), successResult.prevBidAmount(),
                    committedBidderId, committedAmount);
        }
    }

    private void rollbackRedisBidState(BidRedisKeys redisKeys, BidRedisSuccessResult successResult) {
        Long rollbackResult = stringRedisTemplate.execute(
                bidRollbackScript,
                redisKeys.asList(),
                successResult.appliedVersion(),
                successResult.prevBidderId(),
                successResult.prevBidAmount(),
                successResult.previousVersion()
        );

        if (Long.valueOf(1L).equals(rollbackResult)) {
            log.info("[rollbackRedisBidState] Redis 상태 롤백 완료. 적용 버전: {}", successResult.appliedVersion());
            return;
        }

        log.warn("[rollbackRedisBidState] Redis 상태 롤백을 건너뜁니다. 더 새로운 버전이 이미 반영되었을 수 있습니다. 적용 버전: {}",
                successResult.appliedVersion());
    }

    private void saveBidHistory(TransactionFeed feed, User bidder, Long bidAmount, LocalDateTime bidTimeStamp) {
        bidsRepository.save(Bids.builder()
                .transactionFeed(feed)
                .user(bidder)
                .bidAmount(bidAmount)
                .bidTime(bidTimeStamp)
                .build());
    }

    private record BidRedisKeys(String highestPriceKey, String highestBidderKey, String stateVersionKey) {
        private static BidRedisKeys fromFeedId(Long feedId) {
            String keyPrefix = "bids:{" + feedId + "}";
            return new BidRedisKeys(
                    keyPrefix + ":highest_price",
                    keyPrefix + ":highest_bidder_id",
                    keyPrefix + ":state_version"
            );
        }

        private List<String> asList() {
            return List.of(highestPriceKey, highestBidderKey, stateVersionKey);
        }
    }

    private record BidRedisSuccessResult(
            String prevBidderId,
            String prevBidAmount,
            String appliedVersion,
            String previousVersion
    ) {
        private static BidRedisSuccessResult from(List<Object> result) {
            if (result.size() < 5) {
                throw new InternalServerException(ErrorCode.LUA_SCRIPT_ERROR);
            }

            return new BidRedisSuccessResult(
                    String.valueOf(result.get(1)),
                    String.valueOf(result.get(2)),
                    String.valueOf(result.get(3)),
                    String.valueOf(result.get(4))
            );
        }

        private boolean hasPreviousBid() {
            return !RedisConstant.REDIS_NULL_SENTINEL.equals(prevBidderId)
                    && !RedisConstant.REDIS_NULL_SENTINEL.equals(prevBidAmount);
        }
    }
}
