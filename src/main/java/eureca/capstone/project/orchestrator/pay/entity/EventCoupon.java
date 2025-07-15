package eureca.capstone.project.orchestrator.pay.entity;

import jakarta.persistence.*;

@Table(name = "event_coupon")
@Entity
public class EventCoupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_coupon_id")
    private Long eventCouponId;
    @Column(name = "coupon_number")
    private String couponNumber;

    @JoinColumn(name = "pay_type_id")
    @OneToOne(fetch = FetchType.LAZY)
    private PayType payType;

    @Column(name = "discount_rate")
    private Long discountRate;
}
