package eureca.capstone.project.orchestrator.common.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jmeter-bypass")
public record JmeterBypassProperties(
        boolean enabled,
        String testKey
) {
}
