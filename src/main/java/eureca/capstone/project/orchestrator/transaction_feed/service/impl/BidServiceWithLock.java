package eureca.capstone.project.orchestrator.transaction_feed.service.impl;

import eureca.capstone.project.orchestrator.alarm.dto.AlarmCreationDto;
import eureca.capstone.project.orchestrator.alarm.service.impl.NotificationProducer;
import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.common.exception.code.ErrorCode;
import eureca.capstone.project.orchestrator.common.exception.custom.BidException;
import eureca.capstone.project.orchestrator.common.exception.custom.TransactionFeedNotFoundException;
import eureca.capstone.project.orchestrator.common.exception.custom.UserNotFoundException;
import eureca.capstone.project.orchestrator.common.util.SalesTypeManager;
import eureca.capstone.project.orchestrator.common.util.StatusManager;
import eureca.capstone.project.orchestrator.pay.service.UserPayService;
import eureca.capstone.project.orchestrator.transaction_feed.document.TransactionFeedDocument;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.PlaceBidRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.entity.Bids;
import eureca.capstone.project.orchestrator.transaction_feed.entity.SalesType;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.transaction_feed.repository.BidsRepository;
import eureca.capstone.project.orchestrator.transaction_feed.repository.TransactionFeedRepository;
import eureca.capstone.project.orchestrator.transaction_feed.repository.TransactionFeedSearchRepository;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidServiceWithLock {
    private final UserRepository userRepository;
    private final TransactionFeedRepository transactionFeedRepository;
    private final TransactionFeedSearchRepository transactionFeedSearchRepository;
    private final BidsRepository bidsRepository;
    private final UserPayService userPayService;
    private final StatusManager statusManager;
    private final SalesTypeManager salesTypeManager;
    private final NotificationProducer notificationProducer;

    @Transactional
    public void placeBidWithDbLock(String email, PlaceBidRequestDto request) {
        TransactionFeed feed = transactionFeedRepository.findByIdWithLock(request.getTransactionFeedId())
                .orElseThrow(TransactionFeedNotFoundException::new);
        log.info("[placeBidWithDbLock] 입찰 판매글 비관적 락 적용 ID: {}", feed.getTransactionFeedId());

        User bidder = findUserByEmail(email);
        log.info("[placeBidWithDbLock] 사용자 ID: {}, 판매글 ID: {}, 입찰금액: {}", bidder.getUserId(), feed.getTransactionFeedId(), request.getBidAmount());

        validateBidPrecondition(bidder, feed, request.getBidAmount());
        log.info("[placeBidWithDbLock] 입찰 사전 검증 통과");

        Optional<Bids> highestBidOptional = bidsRepository.findHighestBidByFeed(feed);
        log.info("[placeBidWithDbLock] 입찰 최고가 조회");

        if (highestBidOptional.isPresent()) {
            Bids highestBid = highestBidOptional.get();

            if (request.getBidAmount() <= highestBid.getBidAmount()) {
                throw new BidException(ErrorCode.BID_AMOUNT_TOO_LOW);
            }

            if (highestBid.getUser().getUserId().equals(bidder.getUserId())) {
                throw new BidException(ErrorCode.CANNOT_BID_ON_OWN_HIGHEST);
            }
        } else {
            if (request.getBidAmount() < feed.getSalesPrice()) {
                throw new BidException(ErrorCode.BID_AMOUNT_TOO_LOW);
            }
        }

        LocalDateTime bidTimeStamp = LocalDateTime.now();

        if (highestBidOptional.isPresent()) {
            Bids prevHighestBid = highestBidOptional.get();
            userPayService.refundPay(prevHighestBid.getUser(), prevHighestBid.getBidAmount());
            log.info("[placeBidWithDbLock] 이전 입찰자 페이 환불 완료. 사용자 ID: {}, 환불 금액: {}", prevHighestBid.getUser().getUserId(), prevHighestBid.getBidAmount());
        }

        userPayService.usePay(bidder, request.getBidAmount());
        log.info("[placeBidWithDbLock] 새로운 입찰자 페이 사용 완료. 사용자 ID: {}, 사용 금액: {}", bidder.getUserId(), request.getBidAmount());

        saveBidHistory(feed, bidder, request.getBidAmount(), bidTimeStamp);
        log.info("입찰 성공 - 사용자 ID: {}, 게시글 ID: {}, 입찰가: {}", bidder.getUserId(), feed.getTransactionFeedId(), request.getBidAmount());

        updateFeedDocumentHighestPrice(feed.getTransactionFeedId(), request.getBidAmount());

        List<User> participants = bidsRepository.findBidsWithUserByTransactionFeed(feed)
                .stream()
                .map(Bids::getUser)
                .distinct()
                .toList();
        log.info("[handleBidResult] 입찰 참여자 {}명 조회 완료", participants.size());

        for (User participant : participants) {
            log.info("transaction_feed_id: {}", feed.getTransactionFeedId());
            if (participant.getUserId().equals(bidder.getUserId())) {
                notificationProducer.send(AlarmCreationDto.builder()
                        .userId(participant.getUserId())
                        .alarmType("입찰 성공")
                        .transactionFeedId(feed.getTransactionFeedId())
                        .content("'" + feed.getTitle() + "'를(을) (다챠페이)" + request.getBidAmount() + "원에 입찰했습니다.")
                        .build());
            } else {
                notificationProducer.send(AlarmCreationDto.builder()
                        .userId(participant.getUserId())
                        .alarmType("입찰 갱신")
                        .transactionFeedId(feed.getTransactionFeedId())
                        .content(bidder + "님이 '" + feed.getTitle() + "'를(을) (다챠페이)" + request.getBidAmount() + "원에 입찰했습니다.")
                        .build());
            }
        }
        log.info("[placeBidWithDbLock] 입찰 알림 전송 완료");
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }

    private void validateBidPrecondition(User bidder, TransactionFeed feed, Long bidAmount) {
        Status salesStatus = statusManager.getStatus("FEED", "ON_SALE");
        SalesType bidSalesType = salesTypeManager.getBidSaleType();

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
        if (bidAmount % 100 != 0) {
            throw new BidException(ErrorCode.BID_AMOUNT_100_DIVISIBLE);
        }
    }

    private void saveBidHistory(TransactionFeed feed, User bidder, Long bidAmount, LocalDateTime bidTimeStamp) {
        bidsRepository.save(Bids.builder()
                .transactionFeed(feed)
                .user(bidder)
                .bidAmount(bidAmount)
                .bidTime(bidTimeStamp)
                .build());
    }


    private void updateFeedDocumentHighestPrice(Long feedId, Long highestPrice) {
        try {
            TransactionFeedDocument document = transactionFeedSearchRepository.findById(feedId)
                    .orElseThrow(TransactionFeedNotFoundException::new);
            document.updateHighestPrice(highestPrice);
            transactionFeedSearchRepository.save(document);
        } catch (Exception e) {
            log.error("[updateFeedDocumentHighestPrice] Elasticsearch 문서 업데이트 실패. Document ID: {}. Error: {}", feedId, e.getMessage());
        }
    }
}
