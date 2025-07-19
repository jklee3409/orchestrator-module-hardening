package eureca.capstone.project.orchestrator.common.util;

import eureca.capstone.project.orchestrator.common.exception.custom.PayTypeNotFoundException;
import eureca.capstone.project.orchestrator.pay.entity.PayType;
import eureca.capstone.project.orchestrator.pay.repository.PayTypeRepository;
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
public class PayTypeManager {

    private final PayTypeRepository payTypeRepository;
    private Map<String, PayType> payTypeCache;

    @PostConstruct
    public void init() {
        payTypeCache = payTypeRepository.findAll().stream()
                .collect(Collectors.toMap(PayType::getName, Function.identity()));
        log.info("PayTypeManager 로드 완료. 전체 payType: {}", payTypeCache.size());
    }

    /**
     * 결제 수단 이름으로 PayType 객체를 반환하는 메서드
     * @param name 결제 수단 이름 (예: "카드", "토스페이")
     * @return PayType 객체
     */
    public PayType getPayType(String name) {
        return Optional.ofNullable(payTypeCache.get(name))
                .orElseThrow(PayTypeNotFoundException::new);
    }
}
