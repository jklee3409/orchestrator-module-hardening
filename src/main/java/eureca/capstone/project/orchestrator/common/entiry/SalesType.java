package eureca.capstone.project.orchestrator.common.entiry;

import jakarta.persistence.*;

@Table(name = "sales_type")
@Entity
public class SalesType {
    @Column(name = "sales_type_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long SalesTypeId;
    private String name;
}
