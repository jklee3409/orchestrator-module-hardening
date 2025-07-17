package eureca.capstone.project.orchestrator.common.util;

import eureca.capstone.project.orchestrator.transaction_feed.entity.SalesType;
import eureca.capstone.project.orchestrator.transaction_feed.repository.SalesTypeRepository;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Getter
public class SalesTypeManager {
    private static final String NORMAL_SALE_NAME = "일반 판매";
    private static final String BID_SALE_NAME = "입찰 판매";

    private final SalesTypeRepository salesTypeRepository;

    private SalesType normalSaleType;
    private SalesType bidSaleType;

    @PostConstruct
    public void init() {
        List<SalesType> salesTypes = salesTypeRepository.findAll();

        this.normalSaleType = salesTypes.stream()
                .filter(st -> NORMAL_SALE_NAME.equals(st.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(NORMAL_SALE_NAME + " SalesType을 데이터베이스에서 찾을 수 없습니다."));

        this.bidSaleType = salesTypes.stream()
                .filter(st -> BID_SALE_NAME.equals(st.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(BID_SALE_NAME + " SalesType을 데이터베이스에서 찾을 수 없습니다."));

        log.info("SalesTypeManager 로드 및 초기화 완료.");
    }
}