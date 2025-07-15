package eureca.capstone.project.orchestrator.transaction_feed.entity;

import eureca.capstone.project.orchestrator.common.entiry.BaseEntity;
import eureca.capstone.project.orchestrator.common.entiry.Status;
import eureca.capstone.project.orchestrator.common.entiry.TelecomCompany;
import eureca.capstone.project.orchestrator.user.entity.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Table(name = "transaction_feed")
@Entity
public class TransactionFeed extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "transaction_feed_id")
    private Long transactionFeedId;

    @JoinColumn(name = "seller_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @JoinColumn(name = "telecome_company_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private TelecomCompany telecomCompany;

    @JoinColumn(name = "sales_type_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private SalesType salesType;

    @Column(name = "sales_data_amount")
    private Long salesDataAmount;

    @Column(name = "default_image_number")
    private Long defaultImageNumber;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @JoinColumn(name = "status_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Status status;
}
