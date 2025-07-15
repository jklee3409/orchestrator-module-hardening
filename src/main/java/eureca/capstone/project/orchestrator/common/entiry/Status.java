package eureca.capstone.project.orchestrator.common.entiry;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "status")
public class Status {

    @Column(name = "status_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long statusId;

    private String statusName;
}
