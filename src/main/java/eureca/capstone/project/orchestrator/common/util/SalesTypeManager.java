package eureca.capstone.project.orchestrator.common.util;

import eureca.capstone.project.orchestrator.common.exception.custom.SalesTypeNotFoundException;
import eureca.capstone.project.orchestrator.transaction_feed.entity.SalesType;
import eureca.capstone.project.orchestrator.transaction_feed.repository.SalesTypeRepository;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@DependsOn("initComponent")
public class SalesTypeManager {

    private final SalesTypeRepository salesTypeRepository;
    private Map<String, SalesType> salesTypeCache;

    @PostConstruct
    public void init() {
        salesTypeCache = salesTypeRepository.findAll().stream()
                .collect(Collectors.toMap(SalesType::getName, Function.identity()));
        log.info("SalesTypeManager 로드 완료. 전체 salesType: {}", salesTypeCache.size());
    }

    /**
     * 판매 유형 이름을 통해 SalesType 객체를 반환하는 메서드
     * @param name 판매 유형 이름 (예: "NORMAL_SALE")
     * @return SalesType 객체
     */
    public SalesType getSalesType(String name) {
        return Optional.ofNullable(salesTypeCache.get(name))
                .orElseThrow(SalesTypeNotFoundException::new);
    }
}