package eureca.capstone.project.orchestrator.market_statistics.entity;

import eureca.capstone.project.orchestrator.common.entiry.BaseEntity;
import eureca.capstone.project.orchestrator.common.entiry.TelecomCompany;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "market_statistics")
public class MarketStatistics extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "statistics_id")
    private Long statisticsId;

    @JoinColumn(name = "telecom_company_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private TelecomCompany telecomCompany;

    @Column(name = "statics_time")
    private LocalDateTime staticsTime;

    @Column(name = "transaction_amount")
    private Integer transactionAmount;

    @Column(name = "average_price")
    private Integer averagePrice;
}
