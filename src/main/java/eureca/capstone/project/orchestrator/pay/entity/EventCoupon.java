package eureca.capstone.project.orchestrator.pay.entity;

import eureca.capstone.project.orchestrator.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "event_coupon")
public class EventCoupon extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_coupon_id")
    private Long eventCouponId;

    @Column(name = "coupon_number")
    private String couponNumber;

    @Column(name = "coupon_name")
    private String couponName;

    @JoinColumn(name = "pay_type_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private PayType payType;

    @Column(name = "discount_rate")
    private Long discountRate;
}
