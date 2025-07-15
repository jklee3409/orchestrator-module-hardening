package eureca.capstone.project.orchestrator.pay.entity;

import eureca.capstone.project.orchestrator.common.entiry.BaseEntity;
import eureca.capstone.project.orchestrator.transaction_feed.entity.DataTransactionHistory;
import jakarta.persistence.*;

@Entity
@Table(name = "pay_history_detail")
public class PayHistoryDetail extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pay_history_detail_id")
    private Long payHistoryDetailId;

    @JoinColumn(name = "pay_history_id")
    @OneToOne(fetch = FetchType.LAZY)
    private PayHistory payHistory;

    @JoinColumn(name = "data_transaction_history_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private DataTransactionHistory dataTransactionHistory;
}
