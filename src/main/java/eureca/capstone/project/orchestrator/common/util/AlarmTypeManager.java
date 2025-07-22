package eureca.capstone.project.orchestrator.common.util;

import eureca.capstone.project.orchestrator.alarm.entity.AlarmType;
import eureca.capstone.project.orchestrator.alarm.repository.AlarmTypeRepository;
import eureca.capstone.project.orchestrator.common.exception.custom.AlarmTypeNotFoundException;
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
@DependsOn("initComponent")
@RequiredArgsConstructor
public class AlarmTypeManager {
    private final AlarmTypeRepository alarmTypeRepository;
    private Map<String, AlarmType> alarmTypeMap;

    @PostConstruct
    public void init() {
        alarmTypeMap = alarmTypeRepository.findAll().stream()
                .collect(Collectors.toMap(AlarmType::getType, Function.identity()));
        log.info("AlarmTypeManager 로드 완료. 전체 alarmType: {}", alarmTypeMap.size());
    }

    public AlarmType getAlarmType(String type) {
        return Optional.ofNullable(alarmTypeMap.get(type))
                .orElseThrow(AlarmTypeNotFoundException::new);
    }
}
