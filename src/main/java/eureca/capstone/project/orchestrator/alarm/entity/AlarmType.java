package eureca.capstone.project.orchestrator.alarm.entity;

import eureca.capstone.project.orchestrator.common.entiry.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "alarm_type")
public class AlarmType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alarm_type_id")
    private Long alarmTypeId;

    private String type;
}
