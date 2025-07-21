package eureca.capstone.project.orchestrator.transaction_feed.service.impl;

import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.common.exception.code.ErrorCode;
import eureca.capstone.project.orchestrator.common.exception.custom.BidException;
import eureca.capstone.project.orchestrator.common.exception.custom.InternalServerException;
import eureca.capstone.project.orchestrator.common.exception.custom.TransactionFeedNotFoundException;
import eureca.capstone.project.orchestrator.common.exception.custom.UserNotFoundException;
import eureca.capstone.project.orchestrator.common.util.SalesTypeManager;
import eureca.capstone.project.orchestrator.common.util.StatusManager;
import eureca.capstone.project.orchestrator.pay.service.UserPayService;
import eureca.capstone.project.orchestrator.transaction_feed.dto.request.PlaceBidRequestDto;
import eureca.capstone.project.orchestrator.transaction_feed.entity.Bids;
import eureca.capstone.project.orchestrator.transaction_feed.entity.SalesType;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.transaction_feed.repository.BidsRepository;
import eureca.capstone.project.orchestrator.transaction_feed.repository.custom.TransactionFeedRepositoryCustom;
import eureca.capstone.project.orchestrator.transaction_feed.service.BidService;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidServiceImpl implements BidService {
    private final UserRepository userRepository;
    private final TransactionFeedRepositoryCustom transactionFeedRepositoryCustom;
    private final BidsRepository bidsRepository;
    private final UserPayService userPayService;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisScript<List> bidScript;
    private final StatusManager statusManager;
    private final SalesTypeManager salesTypeManager;

    @Override
    @Transactional
    public void placeBid(String email, PlaceBidRequestDto request) {
        User bidder = findUserByEmail(email);
        TransactionFeed feed = findTransactionFeedById(request.getTransactionFeedId());
        log.info("[placeBid] 사용자 ID: {}, 판매글 ID: {}, 입찰금액: {}", bidder.getUserId(), feed.getTransactionFeedId(), request.getBidAmount());

        validateBidPrecondition(bidder, feed, request.getBidAmount());
        log.info("[placeBid] 입찰 사전 검증 통과");

        // 레디스 키 정의
        String highestPriceKey = String.format("bids:%d:highest_price", feed.getTransactionFeedId());
        String highestBidderKey = String.format("bids:%d:highest_bidder_id", feed.getTransactionFeedId());

        // 루아 스크립트 실행
        List result = stringRedisTemplate.execute(
                bidScript,
                List.of(highestPriceKey, highestBidderKey),
                request.getBidAmount().toString(),
                bidder.getUserId().toString()
        );
        log.info("[placeBid] 레디스 스크립트 실행 결과: {}", result);

        handleBidResult(result, feed, bidder, request.getBidAmount());
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }

    private TransactionFeed findTransactionFeedById(Long transactionFeedId) {
        return transactionFeedRepositoryCustom.findById(transactionFeedId)
                .orElseThrow(TransactionFeedNotFoundException::new);
    }

    private void validateBidPrecondition(User bidder, TransactionFeed feed, Long bidAmount) {
        log.info("[validateBidPrecondition] 입찰 사전 검증 시작 - 사용자: {}, 판매글 ID: {}, 입찰가: {}", bidder.getUserId(), feed.getTransactionFeedId(), bidAmount);

        Status salesStatus = statusManager.getStatus("FEED", "ON_SALE");
        SalesType bidSalesType = salesTypeManager.getBidSaleType();

        if (feed.getUser().getUserId().equals(bidder.getUserId())) {
            log.error("[validateBidPrecondition] 판매자는 자신의 판매글에 입찰할 수 없습니다. 사용자 ID: {}, 판매글 ID: {}", bidder.getUserId(),
                    feed.getTransactionFeedId());
            throw new BidException(ErrorCode.SELLER_CANNOT_BID);
        }
        if (!feed.getTelecomCompany().getTelecomCompanyId().equals(bidder.getTelecomCompany().getTelecomCompanyId())) {
            log.error("[validateBidPrecondition] 입찰자는 판매글과 동일한 통신사여야 합니다. 사용자 ID: {}, 판매글 ID: {}", bidder.getUserId(),
                    feed.getTransactionFeedId());
            throw new BidException(ErrorCode.INVALID_TELECOM_COMPANY);
        }
        if (!feed.getStatus().equals(salesStatus)) {
            log.error("[validateBidPrecondition] 판매글이 판매 중이 아닙니다. 사용자 ID: {}, 판매글 ID: {}", bidder.getUserId(),
                    feed.getTransactionFeedId());
            throw new BidException(ErrorCode.AUCTION_NOT_ON_SALE);
        }
        if (!feed.getSalesType().equals(bidSalesType)) {
            log.error("[validateBidPrecondition] 해당 판매글은 입찰 판매글이 아닙니다. 사용자 ID: {}, 판매글 ID: {}", bidder.getUserId(),
                    feed.getTransactionFeedId());
            throw new BidException(ErrorCode.FEED_NOT_AUCTION);
        }
        if (feed.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.error("[validateBidPrecondition] 입찰 판매글이 만료되었습니다. 사용자 ID: {}, 판매글 ID: {}", bidder.getUserId(),
                    feed.getTransactionFeedId());
            throw new BidException(ErrorCode.AUCTION_EXPIRED);
        }
        if (bidAmount <= feed.getSalesPrice()) {
            log.error("[validateBidPrecondition] 입찰 금액이 판매가보다 낮습니다. 사용자 ID: {}, 판매글 ID: {}, 입찰가: {}",
                    bidder.getUserId(), feed.getTransactionFeedId(), bidAmount);
            throw new BidException(ErrorCode.BID_AMOUNT_TOO_LOW);
        }
        if (bidAmount % 100 != 0) {
            log.error("[validateBidPrecondition] 입찰 금액은 100원 단위로 입력해야 합니다. 사용자 ID: {}, 판매글 ID: {}, 입찰가: {}",
                    bidder.getUserId(), feed.getTransactionFeedId(), bidAmount);
            throw new BidException(ErrorCode.BID_AMOUNT_TOO_LOW);
        }
    }

    private void handleBidResult(List<Object> result, TransactionFeed feed, User newBidder, Long bidAmount) {
        if (result == null || result.isEmpty()) {
            log.error("[handleBidResult] 루아 스크립트 반환값이 비어있습니다.");
            throw new InternalServerException(ErrorCode.LUA_SCRIPT_ERROR);
        }

        log.info("[handleBidResult] 입찰 결과 처리 시작 - 결과: {}", result);

        String status = (String) result.get(0).toString();

        switch (Objects.requireNonNull(status)) {
            case "SUCCESS" -> {
                String prevBidderIdStr = (String) result.get(1);
                String prevBidAmountStr = (String) result.get(2);
                log.info("[handleBidResult] 입찰 성공");

                if (!"0".equals(prevBidderIdStr)) {
                    Long prevBidderId = Long.parseLong(prevBidderIdStr);
                    Long prevBidAmount = Long.parseLong(prevBidAmountStr);
                    log.info("[handleBidResult] 이전 입찰자 정보 - ID: {}, 금액: {}", prevBidderId, prevBidAmount);

                    User prevBidder = userRepository.findById(prevBidderId)
                            .orElseThrow(UserNotFoundException::new);

                    userPayService.refundPay(prevBidder, prevBidAmount);
                    log.info("[handleBidResult] 이전 입찰자 페이 환불 완료. 사용자 ID: {}, 환불 금액: {}", prevBidderId, prevBidAmount);
                }

                userPayService.usePay(newBidder, bidAmount);
                log.info("[handleBidResult] 새로운 입찰자 페이 사용 완료. 사용자 ID: {}, 사용 금액: {}", newBidder.getUserId(), bidAmount);

                saveBidHistory(feed, newBidder, bidAmount);
                log.info("입찰 성공 - 사용자 ID: {}, 게시글 ID: {}, 입찰가: {}", newBidder.getUserId(), feed.getTransactionFeedId(), bidAmount);
                // TODO: 입찰 성공 시 알림 기능 추가
            }
            case "BID_TOO_LOW" -> throw new BidException(ErrorCode.BID_AMOUNT_TOO_LOW);
            case "SAME_BIDDER" -> throw new BidException(ErrorCode.CANNOT_BID_ON_OWN_HIGHEST);
            default -> {
                log.error("[handleBidResult] 루아 스크립트 실행 과정에서 예외 발생: {}", result);
                throw new InternalServerException(ErrorCode.LUA_SCRIPT_ERROR);
            }
        }
    }

    private void saveBidHistory(TransactionFeed feed, User bidder, Long bidAmount) {
        bidsRepository.save(Bids.builder()
                .transactionFeed(feed)
                .user(bidder)
                .bidAmount(bidAmount)
                .build());
    }
}