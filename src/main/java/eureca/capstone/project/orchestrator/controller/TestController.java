package eureca.capstone.project.orchestrator.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TestController {
    @GetMapping("/test")
    public String test() {
        log.trace("test");
        log.debug("test");
        log.info("test");
        log.warn("test");
        log.error("test");
        return "test";
    }

    @GetMapping("/healthCheck")
    public String healthCheck() {

        log.trace("healthCheck");
        log.debug("healthCheck");
        log.info("healthCheck");
        log.info("마지막 무중단 배포 테스트");
        log.warn("healthCheck");
        log.error("healthCheck");
        return "healthCheck";
    }
}
