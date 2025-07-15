package eureca.capstone.project.orchestrator.common.entiry;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "telecom_company")
public class TelecomCompany {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long telecomCompanyId;

    private String name;
}
