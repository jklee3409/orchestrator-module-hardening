package eureca.capstone.project.orchestrator.transaction_feed.entity;

import eureca.capstone.project.orchestrator.common.entiry.BaseEntity;
import eureca.capstone.project.orchestrator.user.entity.User;
import jakarta.persistence.*;

@Entity
@Table
public class DataTransactionHistory extends BaseEntity {
    // TODO transaction_at 가 BaseEntity로 인하여 자동생성 되는데 지울지 말지 고민
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_history_id")
    private Long transactionHistoryId;

    @JoinColumn(name = "transaction_feed_id")
    @OneToOne(fetch = FetchType.LAZY)
    private TransactionFeed transactionFeed;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Column(name = "transaction_final_price")
    private Long transactionFinalPrice;

    @Column(name = "is_deleted")
    private Boolean isDeleted;
}
