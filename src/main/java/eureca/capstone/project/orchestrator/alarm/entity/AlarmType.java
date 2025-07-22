package eureca.capstone.project.orchestrator.alarm.entity;

import eureca.capstone.project.orchestrator.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
@Table(name = "alarm_type")
public class AlarmType extends BaseEntity {

    @Id
    @Column(name = "alarm_type_id")
    private Long alarmTypeId;

    private String type;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlarmType that = (AlarmType) o;
        return alarmTypeId.equals(that.alarmTypeId) && type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alarmTypeId, type);
    }
}
