package eureca.capstone.project.orchestrator.health.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
public class HealthCheckController {

    @GetMapping("/healthCheck")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.info("[HealthCheck] /healthCheck endpoint called at {}", LocalDateTime.now());

        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "timestamp", LocalDateTime.now().toString(),
                "message", "orchestrator is up and running"
        ));
    }
}
