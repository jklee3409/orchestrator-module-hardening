package eureca.capstone.project.orchestrator.pay.entity;

import eureca.capstone.project.orchestrator.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pay_type")
public class PayType extends BaseEntity {
    @Id
    @Column(name = "pay_type_id")
    private Long payTypeId;

    private String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PayType payType = (PayType) o;
        return Objects.equals(payTypeId, payType.payTypeId) && Objects.equals(name, payType.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payTypeId, name);
    }
}
