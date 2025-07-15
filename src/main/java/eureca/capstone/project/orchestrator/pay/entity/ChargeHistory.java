package eureca.capstone.project.orchestrator.pay.entity;

import eureca.capstone.project.orchestrator.common.entiry.BaseEntity;
import eureca.capstone.project.orchestrator.common.entiry.Status;
import eureca.capstone.project.orchestrator.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "charge_history")
public class ChargeHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "charge_history_id")
    private Long chargeHistoryId;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @JoinColumn(name = "pay_type_id")
    @OneToOne(fetch = FetchType.LAZY)
    private PayType payType;

    @JoinColumn(name = "user_event_coupon_id")
    @OneToOne(fetch = FetchType.LAZY)
    private UserEventCoupon userEventCoupon;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "payment_key")
    private String paymentKey;

    private Long amount;

    @Column(name = "charge_pay")
    private Long chargePay;

    @Column(name = "discount_amount")
    private Long discountAmount;

    @JoinColumn(name = "status_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Status status;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
