package eureca.capstone.project.orchestrator.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

//    @Enumerated(EnumType.STRING)
////    private TelecomCompany telecomCompany;

    private String email;
    private String password;
    private String nickname;

    @Column(name = "phone_number")
    private String phone;

//    @Enumerated(EnumType.STRING)
////    private Status status;

    private String provider;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void updateUserNickname(String nickname) {
        this.nickname = nickname;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateUserPassword(String password) {
        this.password = password;
        this.updatedAt = LocalDateTime.now();
    }
}
