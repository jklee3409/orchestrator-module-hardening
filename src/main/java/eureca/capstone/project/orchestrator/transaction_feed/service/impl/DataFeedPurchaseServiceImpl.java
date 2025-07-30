package eureca.capstone.project.orchestrator.transaction_feed.service.impl;

import eureca.capstone.project.orchestrator.alarm.dto.AlarmCreationDto;
import eureca.capstone.project.orchestrator.alarm.service.impl.NotificationProducer;
import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.common.exception.code.ErrorCode;
import eureca.capstone.project.orchestrator.common.exception.custom.InternalServerException;
import eureca.capstone.project.orchestrator.common.exception.custom.TransactionFeedNotFoundException;
import eureca.capstone.project.orchestrator.common.exception.custom.UserNotFoundException;
import eureca.capstone.project.orchestrator.common.util.SalesTypeManager;
import eureca.capstone.project.orchestrator.common.util.StatusManager;
import eureca.capstone.project.orchestrator.pay.service.PayHistoryService;
import eureca.capstone.project.orchestrator.pay.service.UserPayService;
import eureca.capstone.project.orchestrator.transaction_feed.document.TransactionFeedDocument;
import eureca.capstone.project.orchestrator.transaction_feed.dto.response.PurchaseResponseDto;
import eureca.capstone.project.orchestrator.transaction_feed.entity.DataTransactionHistory;
import eureca.capstone.project.orchestrator.transaction_feed.entity.SalesType;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.transaction_feed.repository.TransactionFeedRepository;
import eureca.capstone.project.orchestrator.transaction_feed.repository.TransactionFeedSearchRepository;
import eureca.capstone.project.orchestrator.transaction_feed.service.DataCouponService;
import eureca.capstone.project.orchestrator.transaction_feed.service.DataFeedPurchaseService;
import eureca.capstone.project.orchestrator.transaction_feed.service.DataTransactionHistoryService;
import eureca.capstone.project.orchestrator.user.entity.User;
import eureca.capstone.project.orchestrator.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataFeedPurchaseServiceImpl implements DataFeedPurchaseService {
    private final TransactionFeedRepository transactionFeedRepository;
    private final UserRepository userRepository;
    private final UserPayService userPayService;
    private final PayHistoryService payHistoryService;
    private final DataCouponService dataCouponService;
    private final DataTransactionHistoryService dataTransactionHistoryService;
    private final StatusManager statusManager;
    private final SalesTypeManager salesTypeManager;
    private final NotificationProducer notificationProducer;
    private final TransactionFeedSearchRepository transactionFeedSearchRepository;

    @Override
    @Transactional
    public PurchaseResponseDto purchase(String buyerEmail, Long transactionFeedId) {
        log.info("[purchase] 즉시 구매 트랜잭션 시작. 구매자: {}, 판매글 ID: {}", buyerEmail, transactionFeedId);

        TransactionFeed feed = transactionFeedRepository.findByIdWithLock(transactionFeedId)
                .orElseThrow(TransactionFeedNotFoundException::new);
        validatePurchase(feed, buyerEmail);
        log.info("[purchase] 판매글 검증 완료. 판매글 ID: {}, 상태: {}", feed.getTransactionFeedId(), feed.getStatus());

        User buyer = findUserByEmail(buyerEmail);
        User seller = feed.getUser();
        Long price = feed.getSalesPrice();

        userPayService.usePay(buyer, price);
        userPayService.refundPay(seller, price);
        log.info("[purchase] 구매자 {} 의 결제 금액: {}, 판매자 {} 의 지급 금액: {}", buyer.getUserId(), price, seller.getUserId(), price);

        DataTransactionHistory txHistory = dataTransactionHistoryService.createNormalTransactionHistory(buyer, feed);
        log.info("[purchase] 거래 내역 생성 완료. 거래 내역 ID: {}, 판매글 ID: {}", txHistory.getTransactionHistoryId(), feed.getTransactionFeedId());

        dataCouponService.issueDataCoupon(buyer, feed);
        log.info("[purchase] 데이터 쿠폰 발급 완료. 구매자: {}, 판매글 ID: {}", buyer.getUserId(), feed.getTransactionFeedId());

        payHistoryService.createPurchasePayHistory(buyer, -price, txHistory);
        payHistoryService.createSalePayHistory(seller, price, txHistory);
        log.info("[purchase] 페이 변동 내역 생성 완료. 구매자: {}, 판매자: {}, 거래 내역 ID: {}", buyer.getUserId(), seller.getUserId(), txHistory.getTransactionHistoryId());

        Status completedStatus = statusManager.getStatus("FEED", "COMPLETED");
        feed.updateStatus(completedStatus);
        log.info("[purchase] 판매글 상태 업데이트 완료. 판매글 ID: {}, 새로운 상태: {}", feed.getTransactionFeedId(), completedStatus.getCode());

        transactionFeedSearchRepository.save(TransactionFeedDocument.fromEntity(feed));
        log.info("[purchase] ES Document 상태 업데이트 완료. Document ID: {}", feed.getTransactionFeedId());

        notificationProducer.send(AlarmCreationDto.builder()
                .userId(buyer.getUserId())
                .alarmType("구매")
                .transactionFeedId(feed.getTransactionFeedId())
                .content("'" + feed.getTitle() + "' 를(을) (다챠페이)" + price + "원에 구매하였습니다.")
                .build());
        log.info("[purchase] 구매자 알림 생성 완료. 구매자: {}, 판매글 ID: {}", buyer.getUserId(), feed.getTransactionFeedId());

        notificationProducer.send(AlarmCreationDto.builder()
                .userId(seller.getUserId())
                .alarmType("판매")
                .transactionFeedId(feed.getTransactionFeedId())
                .content(buyer.getNickname() + "님이 '" + feed.getTitle() + "' 를(을) (다챠페이)" + price + "원에 구매하였습니다.")
                .build());
        log.info("[purchase] 판매자 알림 생성 완료. 판매자: {}, 판매글 ID: {}", seller.getUserId(), feed.getTransactionFeedId());

        return PurchaseResponseDto.builder()
                .transactionFeedId(feed.getTransactionFeedId())
                .dataTransactionHistoryId(txHistory.getTransactionHistoryId())
                .price(price)
                .build();
    }

    @Override
    @Transactional
    public PurchaseResponseDto purchaseAuction(User buyer, TransactionFeed feed, Long finalBidAmount) {
        log.info("[purchaseAuction] 경매 낙찰 처리 시작. 구매자: {}, 판매글: {}, 최종가: {}", buyer.getUserId(), feed.getTransactionFeedId(), finalBidAmount);

        User seller = feed.getUser();

        userPayService.refundPay(seller, finalBidAmount);
        log.info("[purchaseAuction] 판매자 {} 의 지급 금액: {}", seller.getUserId(), finalBidAmount);

        DataTransactionHistory txHistory = dataTransactionHistoryService.createAuctionTransactionHistory(buyer, feed, finalBidAmount);

        dataCouponService.issueDataCoupon(buyer, feed);
        log.info("[purchaseAuction] 데이터 쿠폰 발급 완료. 구매자: {}, 판매글 ID: {}", buyer.getUserId(), feed.getTransactionFeedId());

        payHistoryService.createPurchasePayHistory(buyer, -finalBidAmount, txHistory);
        payHistoryService.createSalePayHistory(seller, finalBidAmount, txHistory);
        log.info("[purchaseAuction] 페이 변동 내역 생성 완료. 구매자: {}, 판매자: {}, 거래 내역 ID: {}", buyer.getUserId(), seller.getUserId(), txHistory.getTransactionHistoryId());

        Status completedStatus = statusManager.getStatus("FEED", "COMPLETED");
        feed.updateStatus(completedStatus);
        log.info("[purchaseAuction] 판매글 상태 업데이트 완료. 판매글 ID: {}, 새로운 상태: {}", feed.getTransactionFeedId(), completedStatus.getCode());

        transactionFeedSearchRepository.save(TransactionFeedDocument.fromEntity(feed));
        log.info("[purchaseAuction] ES Document 상태 업데이트 완료. Document ID: {}", feed.getTransactionFeedId());

        return PurchaseResponseDto.builder()
                .transactionFeedId(feed.getTransactionFeedId())
                .dataTransactionHistoryId(txHistory.getTransactionHistoryId())
                .price(finalBidAmount)
                .build();
    }

    private void validatePurchase(TransactionFeed feed, String buyerEmail) {
        User buyer = findUserByEmail(buyerEmail);
        Status onSaleStatus = statusManager.getStatus("FEED", "ON_SALE");
        SalesType auctionSalesType = salesTypeManager.getBidSaleType();

        if (!feed.getStatus().equals(onSaleStatus)) {
            log.error("[validatePurchase] 판매글이 판매 중이 아닙니다. 판매글 ID: {}, 상태: {}", feed.getTransactionFeedId(), feed.getStatus().getCode());
            throw new InternalServerException(ErrorCode.FEED_NOT_ON_SALE);
        }

        if (feed.isDeleted()) {
            log.error("[validatePurchase] 판매글이 삭제되었습니다. 판매글 ID: {}", feed.getTransactionFeedId());
            throw new TransactionFeedNotFoundException();
        }

        if (feed.getSalesType().equals(auctionSalesType)) {
            log.error("[validatePurchase] 경매 판매글은 즉시 구매할 수 없습니다. 판매글 ID: {}", feed.getTransactionFeedId());
            throw new InternalServerException(ErrorCode.CANNOT_BUY_AUCTION_FEED);
        }

        if (!feed.getTelecomCompany().getTelecomCompanyId().equals(buyer.getTelecomCompany().getTelecomCompanyId())) {
            log.error("[validatePurchase] 구매자가 판매글의 통신사와 일치하지 않습니다. 판매글 ID: {}, 구매자 이메일: {}, 판매글 통신사: {}, 구매자 통신사: {}",
                      feed.getTransactionFeedId(), buyerEmail, feed.getTelecomCompany().getTelecomCompanyId(), buyer.getTelecomCompany().getTelecomCompanyId());
            throw new InternalServerException(ErrorCode.INVALID_TELECOM_COMPANY);
        }

        if (feed.getUser().getEmail().equals(buyerEmail)) {
            log.error("[validatePurchase] 사용자가 자신의 판매글을 구매하려고 시도했습니다. 판매글 ID: {}, 사용자 이메일: {}", feed.getTransactionFeedId(), buyerEmail);
            throw new InternalServerException(ErrorCode.CANNOT_BUY_OWN_FEED);
        }
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
    }
}
