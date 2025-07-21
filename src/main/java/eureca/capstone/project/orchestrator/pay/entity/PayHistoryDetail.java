package eureca.capstone.project.orchestrator.pay.entity;

import eureca.capstone.project.orchestrator.common.entity.BaseEntity;
import eureca.capstone.project.orchestrator.transaction_feed.entity.DataTransactionHistory;
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
