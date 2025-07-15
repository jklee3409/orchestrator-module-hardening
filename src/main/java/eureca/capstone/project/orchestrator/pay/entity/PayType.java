package eureca.capstone.project.orchestrator.pay.entity;

import jakarta.persistence.*;

@Table(name = "pay_type")
@Entity
public class PayType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pay_type_id")
    private Long payTypeId;

    private String name;
}
