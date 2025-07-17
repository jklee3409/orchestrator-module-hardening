package eureca.capstone.project.orchestrator.user.entity;

import eureca.capstone.project.orchestrator.common.entity.BaseEntity;
import eureca.capstone.project.orchestrator.common.entity.Status;
import eureca.capstone.project.orchestrator.common.entity.TelecomCompany;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "user",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_email", columnNames = "email")
        }
)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @JoinColumn(name = "telecom_company_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private TelecomCompany telecomCompany;

    @Column(unique = true)
    private String email;
    private String password;
    private String nickname;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Setter
    @JoinColumn(name = "status_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Status status;

    private String provider;

    public void updateUserNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateUserPassword(String password) {
        this.password = password;
    }
}
