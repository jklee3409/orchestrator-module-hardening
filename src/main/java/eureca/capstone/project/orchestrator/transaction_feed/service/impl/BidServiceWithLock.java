package eureca.capstone.project.orchestrator.transaction_feed.service.impl;

import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.common.exception.code.ErrorCode;
import eureca.capstone.project.orchestrator.common.exception.custom.BidException;
import eureca.capstone.project.orchestrator.common.exception.custom.TransactionFeedNotFoundException;
import eureca.capstone.project.orchestrator.common.exception.custom.UserNotFoundException;
import eureca.capstone.project.orchestrator.common.util.SalesTypeManager;
import eureca.capstone.project.orchestrator.common.util.StatusManager;
import eureca.capstone.project.orchestrator.pay.entity.UserPay;
import eureca.capstone.project.orchestrator.pay.repository.UserPayRepository;
import eureca.capstone.project.orchestrator.pay.service.UserPayService;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.PlaceBidRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.entity.Bids;
import eureca.capstone.project.orchestrator.transaction_feed.entity.SalesType;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.transaction_feed.event.BidSucceededEvent;
import eureca.capstone.project.orchestrator.transaction_feed.repository.BidsRepository;
import eureca.capstone.project.orchestrator.transaction_feed.repository.TransactionFeedRepository;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidServiceWithLock {
    private final UserRepository userRepository;
    private final TransactionFeedRepository transactionFeedRepository;
    private final BidsRepository bidsRepository;
    private final UserPayService userPayService;
    private final UserPayRepository userPayRepository;
    private final StatusManager statusManager;
    private final SalesTypeManager salesTypeManager;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void placeBidWithDbLock(String email, PlaceBidRequestDto request) {
        User bidder = findUserByEmail(email);
        TransactionFeed lockedFeed = findTransactionFeedByIdWithLock(request.getTransactionFeedId());
        log.info("[placeBidWithDbLock] userId={}, feedId={}, bidAmount={}",
                bidder.getUserId(), lockedFeed.getTransactionFeedId(), request.getBidAmount());

        validateBidPrecondition(bidder, lockedFeed, request.getBidAmount());

        LocalDateTime bidTimeStamp = LocalDateTime.now();
        Optional<Bids> currentHighestBid = bidsRepository
                .findTopByTransactionFeedOrderByBidAmountDescBidTimeDesc(lockedFeed);

        validateAgainstCommittedHighest(currentHighestBid, bidder, request.getBidAmount());
        currentHighestBid.ifPresent(this::refundCommittedHighestBid);

        userPayService.usePay(bidder, request.getBidAmount());
        saveBidHistory(lockedFeed, bidder, request.getBidAmount(), bidTimeStamp);

        applicationEventPublisher.publishEvent(BidSucceededEvent.of(lockedFeed, bidder, request.getBidAmount()));
        log.info("[placeBidWithDbLock] AFTER_COMMIT follow-up event published. feedId={}, userId={}, bidAmount={}",
                lockedFeed.getTransactionFeedId(), bidder.getUserId(), request.getBidAmount());
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }

    private TransactionFeed findTransactionFeedByIdWithLock(Long transactionFeedId) {
        return transactionFeedRepository.findByIdWithLock(transactionFeedId)
                .orElseThrow(TransactionFeedNotFoundException::new);
    }

    private void validateBidPrecondition(User bidder, TransactionFeed feed, Long bidAmount) {
        Status salesStatus = statusManager.getStatus("FEED", "ON_SALE");
        SalesType bidSalesType = salesTypeManager.getBidSaleType();
        UserPay userPay = userPayRepository.findByUserId(bidder.getUserId())
                .orElseThrow(UserNotFoundException::new);
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

    private void saveBidHistory(TransactionFeed feed, User bidder, Long bidAmount, LocalDateTime bidTimeStamp) {
        bidsRepository.save(Bids.builder()
                .transactionFeed(feed)
                .user(bidder)
                .bidAmount(bidAmount)
                .bidTime(bidTimeStamp)
                .build());
    }
}
