package eureca.capstone.project.orchestrator.pay.entity;

import eureca.capstone.project.orchestrator.common.entity.BaseEntity;
import eureca.capstone.project.orchestrator.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "exchange_history")
public class ExchangeHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exchange_history_id")
    private Long exchangeHistoryId;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @JoinColumn(name = "bank_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Bank bank;

    @Column(name = "exchange_account")
    private String exchangeAccount;

    private Long amount;
    private Long fee;

    @Column(name = "final_amount")
    private Long finalAmount;
}
