package eureca.capstone.project.orchestrator.pay.entity;

import eureca.capstone.project.orchestrator.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "bank")
public class Bank extends BaseEntity {

    @Id
    private Long bankId;

    private String bankName;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Bank bank = (Bank) o;
        return Objects.equals(bankId, bank.bankId) && Objects.equals(bankName, bank.bankName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bankId, bankName);
    }
}
