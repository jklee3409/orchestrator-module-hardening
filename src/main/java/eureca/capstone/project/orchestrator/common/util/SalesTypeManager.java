package eureca.capstone.project.orchestrator.common.util;

import eureca.capstone.project.orchestrator.common.exception.custom.SalesTypeNotFoundException;
import eureca.capstone.project.orchestrator.transaction_feed.entity.SalesType;
import eureca.capstone.project.orchestrator.transaction_feed.repository.SalesTypeRepository;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Getter
@DependsOn("initComponent")
public class SalesTypeManager {
    private static final Long NORMAL_SALE_ID = 1L;
    private static final Long BID_SALE_ID = 2L;

    private final SalesTypeRepository salesTypeRepository;

    private SalesType normalSaleType;
    private SalesType bidSaleType;

    @PostConstruct
    public void init() {
        this.normalSaleType = salesTypeRepository.findById(NORMAL_SALE_ID)
                .orElseThrow(SalesTypeNotFoundException::new);

        this.bidSaleType = salesTypeRepository.findById(BID_SALE_ID)
                .orElseThrow(SalesTypeNotFoundException::new);

        log.info("SalesTypeManager 로드 및 초기화 완료.");
    }
}