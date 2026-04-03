package eureca.capstone.project.orchestrator.common.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.cookie")
public record AppCookieProperties(
        boolean secure,
        String sameSite,
        String domain
) {
}
