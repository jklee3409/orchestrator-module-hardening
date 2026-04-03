package eureca.capstone.project.orchestrator.common.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.urls")
public record AppUrlProperties(
        String verifyEmailBase,
        String frontendVerifyComplete,
        String resetPasswordBase,
        String oauthSuccessRedirect
) {
}
