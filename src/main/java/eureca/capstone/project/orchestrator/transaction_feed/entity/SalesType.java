package eureca.capstone.project.orchestrator.transaction_feed.entity;

import eureca.capstone.project.orchestrator.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Table(name = "sales_type")
@Entity
@Getter
public class SalesType extends BaseEntity {
    @Column(name = "sales_type_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long SalesTypeId;
    private String name;
}
