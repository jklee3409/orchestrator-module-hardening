package eureca.capstone.project.orchestrator.pay.entity;

import eureca.capstone.project.orchestrator.common.entiry.BaseEntity;
import eureca.capstone.project.orchestrator.user.entity.User;
import jakarta.persistence.*;

@Entity
@Table(name = "pay_history")
public class PayHistory extends BaseEntity {
    // TODO 변동 시간을 BaseEntity로 대체하는것은 어떠한가
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pay_history_id")
    private Long payHistoryId;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @JoinColumn(name = "change_type_id")
    @OneToOne(fetch = FetchType.LAZY)
    private ChangeType changeType;

    @Column(name = "changed_pay")
    private Long changedPay;

    @Column(name = "final_pay")
    private Long finalPay;




}
