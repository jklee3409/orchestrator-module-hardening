package eureca.capstone.project.orchestrator.user.entity;

import eureca.capstone.project.orchestrator.common.entity.BaseEntity;
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
@Table(name = "user_data")
public class UserData extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userDataId;

    private Long userId;
    private Long planId;
    private Long totalDataMb; // 총 소유 데이터
    private Long sellableDataMb; // 판매 가능한 데이터
    private Long buyerDataMb; // 구매한 데이터
    private Integer resetDataAt; // 데이터 초기화 날짜

    public void createSellableData(Long amount) {
        this.totalDataMb -= amount;
        this.sellableDataMb += amount;
    }

    public void deductSellableData(Long amount) {
        this.sellableDataMb -= amount;
    }

    public void addSellableData(Long amount) {
        this.sellableDataMb += amount;
    }

    public void addBuyerData(Long amount) {
        this.buyerDataMb += amount;
    }

    public void deductBuyerData(Long amount) {
        this.buyerDataMb -= amount;
    }
}
