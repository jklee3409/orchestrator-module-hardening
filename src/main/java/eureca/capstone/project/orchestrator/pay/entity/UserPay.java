package eureca.capstone.project.orchestrator.pay.entity;

import eureca.capstone.project.orchestrator.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Table(name = "user_pay")
@Entity
public class UserPay {
    @Id
    @OneToOne
    private User user;
    private Long pay;
}
