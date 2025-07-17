package eureca.capstone.project.orchestrator.pay.entity;

import eureca.capstone.project.orchestrator.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;


@Entity
@Getter
@Table(name = "pay_type")
public class PayType extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pay_type_id")
    private Long payTypeId;

    private String name;
}
