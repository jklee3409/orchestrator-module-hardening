package eureca.capstone.project.orchestrator.pay.entity;

import eureca.capstone.project.orchestrator.common.entiry.BaseEntity;
import jakarta.persistence.*;

@Table(name = "pay_type")
@Entity
public class PayType extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pay_type_id")
    private Long payTypeId;

    private String name;
}
