package eureca.capstone.project.orchestrator.transaction_feed.entity;

import eureca.capstone.project.orchestrator.common.entiry.TelecomCompany;
import jakarta.persistence.*;

@Entity
@Table(name = "data_coupon")
public class DataCoupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "data_coupon_id")
    private Long dataCouponId;
    @Column(name = "coupon_number")
    private String couponNumber;
    @Column(name = "data_amount")
    private Long dataAmount;
    @ManyToOne(fetch = FetchType.LAZY)
    private TelecomCompany telecomCompany;
}
