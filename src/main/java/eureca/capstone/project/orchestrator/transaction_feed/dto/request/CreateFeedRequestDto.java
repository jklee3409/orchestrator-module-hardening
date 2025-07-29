package eureca.capstone.project.orchestrator.transaction_feed.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
public class CreateFeedRequestDto {

    @NotBlank(message = "제목은 공백일 수 없습니다")
    String title;

    @NotBlank(message = "내용은 공백일 수 없습니다")
    String content;

    @NotNull(message = "통신사는 필수입니다")
    Long telecomCompanyId;

    @NotNull(message = "판매 유형은 필수입니다")
    Long salesTypeId;

    @NotNull(message = "판매 가격은 필수입니다")
    @Positive(message = "판매 가격은 양수여야 합니다")
    Long salesPrice;

    @NotNull(message = "판매 데이터 양은 필수입니다")
    @Positive(message = "판매 데이터 양은 양수여야 합니다")
    Long salesDataAmount;

    Long defaultImageNumber;
}
