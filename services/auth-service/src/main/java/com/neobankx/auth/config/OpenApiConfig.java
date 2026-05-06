package com.neobankx.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI authOpenApi() {
        String schemeName = "bearer-jwt";
        return new OpenAPI()
                .info(new Info()
                        .title("NeoBankX Auth Service")
                        .version("0.1.0")
                        .description("Registration, login, refresh token rotation, logout, and RBAC token claims."))
                .schemaRequirement(schemeName, new SecurityScheme()
                        .name(schemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"))
                .addSecurityItem(new SecurityRequirement().addList(schemeName));
    }
}

