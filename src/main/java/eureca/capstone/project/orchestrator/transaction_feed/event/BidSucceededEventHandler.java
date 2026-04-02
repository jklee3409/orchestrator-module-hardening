package eureca.capstone.project.orchestrator.transaction_feed.event;

import eureca.capstone.project.orchestrator.alarm.dto.AlarmCreationDto;
import eureca.capstone.project.orchestrator.alarm.service.impl.NotificationProducer;
import eureca.capstone.project.orchestrator.common.exception.custom.TransactionFeedNotFoundException;
import eureca.capstone.project.orchestrator.transaction_feed.document.TransactionFeedDocument;
import eureca.capstone.project.orchestrator.transaction_feed.entity.Bids;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.transaction_feed.repository.BidsRepository;
import eureca.capstone.project.orchestrator.transaction_feed.repository.TransactionFeedRepository;
import eureca.capstone.project.orchestrator.transaction_feed.repository.TransactionFeedSearchRepository;
import eureca.capstone.project.orchestrator.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class BidSucceededEventHandler {

    private final TransactionFeedRepository transactionFeedRepository;
    private final TransactionFeedSearchRepository transactionFeedSearchRepository;
    private final BidsRepository bidsRepository;
    private final NotificationProducer notificationProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(BidSucceededEvent event) {
        log.info("[BidSucceededEventHandler] AFTER_COMMIT 입찰 후속 처리 시작. 판매글 ID: {}, 입찰자 ID: {}, 입찰가: {}",
                event.transactionFeedId(), event.bidderUserId(), event.bidAmount());

        updateFeedDocumentHighestPrice(event.transactionFeedId());
        sendBidNotifications(event);
    }

    private void updateFeedDocumentHighestPrice(Long feedId) {
        try {
            TransactionFeed feed = transactionFeedRepository.findById(feedId)
                    .orElseThrow(TransactionFeedNotFoundException::new);

            Long committedHighestPrice = bidsRepository.findTopByTransactionFeedOrderByBidAmountDescBidTimeDesc(feed)
                    .map(Bids::getBidAmount)
                    .orElse(feed.getSalesPrice());

            TransactionFeedDocument document = transactionFeedSearchRepository.findById(feedId)
                    .orElseThrow(TransactionFeedNotFoundException::new);

            Long indexedHighestPrice = document.getCurrentHighestPrice() != null
                    ? document.getCurrentHighestPrice()
                    : feed.getSalesPrice();
            Long highestPriceToIndex = Math.max(indexedHighestPrice, committedHighestPrice);

            if (!Objects.equals(document.getCurrentHighestPrice(), highestPriceToIndex)) {
                document.updateHighestPrice(highestPriceToIndex);
                transactionFeedSearchRepository.save(document);
            }

            log.info("[BidSucceededEventHandler] Elasticsearch 문서 업데이트 완료. Document ID: {}, Indexed Highest Price: {}, Committed Highest Price: {}, Stored Highest Price: {}",
                    feedId, indexedHighestPrice, committedHighestPrice, highestPriceToIndex);
        } catch (Exception e) {
            log.error("[BidSucceededEventHandler] Elasticsearch 문서 업데이트 실패. Document ID: {}. Error: {}",
                    feedId, e.getMessage(), e);
        }
    }

    private void sendBidNotifications(BidSucceededEvent event) {
        try {
            TransactionFeed feed = transactionFeedRepository.findById(event.transactionFeedId())
                    .orElseThrow(TransactionFeedNotFoundException::new);

            List<User> participants = bidsRepository.findBidsWithUserByTransactionFeed(feed)
                    .stream()
                    .map(Bids::getUser)
                    .distinct()
                    .toList();

            log.info("[BidSucceededEventHandler] 입찰 참여자 {}명 조회 완료", participants.size());

            for (User participant : participants) {
                try {
                    if (participant.getUserId().equals(event.bidderUserId())) {
                        notificationProducer.send(AlarmCreationDto.builder()
                                .userId(participant.getUserId())
                                .alarmType("입찰 성공")
                                .transactionFeedId(event.transactionFeedId())
                                .content("[" + event.feedTitle() + "]를(을) (다챠페이)" + event.bidAmount() + "원에 입찰했습니다.")
                                .build());
                    } else {
                        notificationProducer.send(AlarmCreationDto.builder()
                                .userId(participant.getUserId())
                                .alarmType("입찰 갱신")
                                .transactionFeedId(event.transactionFeedId())
                                .content(event.bidderNickname() + "님이 [" + event.feedTitle() + "]를(을) (다챠페이)" + event.bidAmount() + "원에 입찰했습니다.")
                                .build());
                    }
                } catch (Exception e) {
                    log.error("[BidSucceededEventHandler] 개별 알림 전송 실패. 대상 사용자 ID: {}, 판매글 ID: {}",
                            participant.getUserId(), event.transactionFeedId(), e);
                }
            }

            log.info("[BidSucceededEventHandler] 입찰 알림 전송 완료. 판매글 ID: {}", event.transactionFeedId());
        } catch (Exception e) {
            log.error("[BidSucceededEventHandler] 입찰 알림 후속 처리 실패. 판매글 ID: {}",
                    event.transactionFeedId(), e);
        }
    }
}
