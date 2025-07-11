package eureca.capstone.project.orchestrator.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/orchestrator")
public class GatewayTestController {
    @GetMapping("/test")
    public String test() {
        return "test";
    }
}
