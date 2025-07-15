package eureca.capstone.project.orchestrator.pay.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "change_type")
public class ChangeType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "change_type_id")
    private Long changeTypeId;

    private String type;
    private String content;
}
