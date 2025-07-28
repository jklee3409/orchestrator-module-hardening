package eureca.capstone.project.orchestrator.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // BearerAuth (Authorization 헤더)
        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        // CookieAuth (refreshToken 쿠키)
        SecurityScheme cookieScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE)
                .name("refreshToken");

        return new OpenAPI()
                .info(new Info()
                        .title("Todolist API")
                        .description("Todolist Application API Documentation")
                        .version("v1.0"))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .addSecurityItem(new SecurityRequirement().addList("cookieAuth"))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", bearerScheme)
                        .addSecuritySchemes("cookieAuth", cookieScheme)
                );
    }
}