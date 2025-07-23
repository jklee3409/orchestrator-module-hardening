package eureca.capstone.project.orchestrator.transaction_feed.entity;

import eureca.capstone.project.orchestrator.common.entity.BaseEntity;
import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.user.entity.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;
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
@Table(name = "user_data_coupon")
public class UserDataCoupon extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_data_coupon_id")
    private Long userDataCouponId;

    @JoinColumn(name = "data_coupon_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private DataCoupon dataCoupon;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @JoinColumn(name = "status_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Status status;

    public void updateStatus(Status status) {
        this.status = status;
    }
}
