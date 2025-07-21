package eureca.capstone.project.orchestrator.transaction_feed.entity;

import eureca.capstone.project.orchestrator.common.entity.BaseEntity;
import eureca.capstone.project.orchestrator.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "bids")
public class Bids extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bids_id")
    private Long bidsId;

    @JoinColumn(name = "transaction_feed_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private TransactionFeed transactionFeed;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Column(name = "bid_amount")
    private Long bidAmount;
}
