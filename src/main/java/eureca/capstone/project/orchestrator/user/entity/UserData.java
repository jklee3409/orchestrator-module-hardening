package eureca.capstone.project.orchestrator.user.entity;

import eureca.capstone.project.orchestrator.common.entiry.BaseEntity;
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
    private Integer totalDataMb; // 총 소유 데이터
    private Integer sellableDataMb; // 판매 가능한 데이터
    private Integer buyerDataMb; // 구매한 데이터
    private Integer resetDataAt; // 데이터 초기화 날짜

    public void createSellableData(Integer amount) {
        this.totalDataMb -= amount;
        this.sellableDataMb += amount;
    }

    public void deductSellableData(Integer amount) {
        this.sellableDataMb -= amount;
    }

    public void addSellableData(Integer amount) {
        this.sellableDataMb += amount;
    }

    public void addBuyerData(Integer amount) {
        this.buyerDataMb += amount;
    }

    public void deductBuyerData(Integer amount) {
        this.buyerDataMb -= amount;
    }
}
