package eureca.capstone.project.orchestrator.transaction_feed.event;

import eureca.capstone.project.orchestrator.alarm.service.impl.NotificationProducer;
import eureca.capstone.project.orchestrator.transaction_feed.document.TransactionFeedDocument;
import eureca.capstone.project.orchestrator.transaction_feed.entity.Bids;
import eureca.capstone.project.orchestrator.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.orchestrator.transaction_feed.repository.BidsRepository;
import eureca.capstone.project.orchestrator.transaction_feed.repository.TransactionFeedRepository;
import eureca.capstone.project.orchestrator.transaction_feed.repository.TransactionFeedSearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BidSucceededEventHandlerTest {

    private static final Long FEED_ID = 100L;
    private static final Long SALES_PRICE = 10_000L;
    private static final Long EVENT_BID_AMOUNT = 12_000L;
    private static final Long COMMITTED_HIGHEST_PRICE = 15_000L;
    private static final Long STALE_INDEXED_PRICE = 20_000L;

    private TransactionFeedRepository transactionFeedRepository;
    private TransactionFeedSearchRepository transactionFeedSearchRepository;
    private BidsRepository bidsRepository;
    private NotificationProducer notificationProducer;

    private BidSucceededEventHandler handler;

    @BeforeEach
    void setUp() {
        transactionFeedRepository = mock(TransactionFeedRepository.class);
        transactionFeedSearchRepository = mock(TransactionFeedSearchRepository.class);
        bidsRepository = mock(BidsRepository.class);
        notificationProducer = mock(NotificationProducer.class);

        handler = new BidSucceededEventHandler(
                transactionFeedRepository,
                transactionFeedSearchRepository,
                bidsRepository,
                notificationProducer
        );
    }

    @Test
    @DisplayName("AFTER_COMMIT corrects the Elasticsearch highest bid back to the committed DB highest bid")
    void afterCommitUsesCommittedHighestBidAsSearchDocumentSourceOfTruth() {
        TransactionFeed feed = mock(TransactionFeed.class);
        when(feed.getSalesPrice()).thenReturn(SALES_PRICE);

        Bids committedHighestBid = mock(Bids.class);
        when(committedHighestBid.getBidAmount()).thenReturn(COMMITTED_HIGHEST_PRICE);

        TransactionFeedDocument indexedDocument = TransactionFeedDocument.builder()
                .id(FEED_ID)
                .salesPrice(SALES_PRICE)
                .currentHighestPrice(STALE_INDEXED_PRICE)
                .sortPrice(STALE_INDEXED_PRICE)
                .build();

        when(transactionFeedRepository.findById(FEED_ID)).thenReturn(Optional.of(feed));
        when(bidsRepository.findTopByTransactionFeedOrderByBidAmountDescBidTimeDesc(feed))
                .thenReturn(Optional.of(committedHighestBid));
        when(bidsRepository.findBidsWithUserByTransactionFeed(feed)).thenReturn(List.of());
        when(transactionFeedSearchRepository.findById(FEED_ID)).thenReturn(Optional.of(indexedDocument));

        handler.handle(new BidSucceededEvent(FEED_ID, 1L, "bidder", "feed", EVENT_BID_AMOUNT));

        ArgumentCaptor<TransactionFeedDocument> documentCaptor =
                ArgumentCaptor.forClass(TransactionFeedDocument.class);
        verify(transactionFeedSearchRepository).save(documentCaptor.capture());

        assertThat(documentCaptor.getValue().getCurrentHighestPrice()).isEqualTo(COMMITTED_HIGHEST_PRICE);
        assertThat(documentCaptor.getValue().getSortPrice()).isEqualTo(COMMITTED_HIGHEST_PRICE);
    }

    @Test
    @DisplayName("AFTER_COMMIT skips the Elasticsearch write when the indexed value already matches the committed highest bid")
    void afterCommitSkipsWriteWhenIndexedHighestPriceIsAlreadyCurrent() {
        TransactionFeed feed = mock(TransactionFeed.class);
        when(feed.getSalesPrice()).thenReturn(SALES_PRICE);

        Bids committedHighestBid = mock(Bids.class);
        when(committedHighestBid.getBidAmount()).thenReturn(COMMITTED_HIGHEST_PRICE);

        TransactionFeedDocument indexedDocument = TransactionFeedDocument.builder()
                .id(FEED_ID)
                .salesPrice(SALES_PRICE)
                .currentHighestPrice(COMMITTED_HIGHEST_PRICE)
                .sortPrice(COMMITTED_HIGHEST_PRICE)
                .build();

        when(transactionFeedRepository.findById(FEED_ID)).thenReturn(Optional.of(feed));
        when(bidsRepository.findTopByTransactionFeedOrderByBidAmountDescBidTimeDesc(feed))
                .thenReturn(Optional.of(committedHighestBid));
        when(bidsRepository.findBidsWithUserByTransactionFeed(feed)).thenReturn(List.of());
        when(transactionFeedSearchRepository.findById(FEED_ID)).thenReturn(Optional.of(indexedDocument));

        handler.handle(new BidSucceededEvent(FEED_ID, 1L, "bidder", "feed", EVENT_BID_AMOUNT));

        verify(transactionFeedSearchRepository, never()).save(any(TransactionFeedDocument.class));
    }
}
