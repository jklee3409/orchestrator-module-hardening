package eureca.capstone.project.orchestrator.pay.entity;

import eureca.capstone.project.orchestrator.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "charge_history_detail")
public class ChargeHistoryDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "charge_history_detail_id")
    private Long chargeHistoryDetailId;

    @JoinColumn(name = "pay_history_id")
    @OneToOne(fetch = FetchType.LAZY)
    private PayHistory payHistory;

    @JoinColumn(name = "charge_history_id")
    @OneToOne(fetch = FetchType.LAZY)
    private ChargeHistory chargeHistory;
}
