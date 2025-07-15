package eureca.capstone.project.orchestrator.pay.entity;

import eureca.capstone.project.orchestrator.common.entiry.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "change_type")
public class ChangeType extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "change_type_id")
    private Long changeTypeId;

    private String type;
    private String content;
}
