package eureca.capstone.project.orchestrator.pay.dto;

import eureca.capstone.project.orchestrator.pay.entity.PayType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PayTypeDto {
    private Long payTypeId;
    private String name;

    public static PayTypeDto fromEntity(PayType payType) {
        return PayTypeDto.builder()
                .payTypeId(payType.getPayTypeId())
                .name(payType.getName())
                .build();
    }
}
