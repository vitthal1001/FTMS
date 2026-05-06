package com.neobankx.account.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI accountOpenApi() {
        String schemeName = "bearer-jwt";
        return new OpenAPI()
                .info(new Info()
                        .title("NeoBankX Account Service")
                        .version("0.1.0")
                        .description("Account lifecycle, ledger postings, balance snapshots, and replay-safe transfer foundation."))
                .schemaRequirement(schemeName, new SecurityScheme()
                        .name(schemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"))
                .addSecurityItem(new SecurityRequirement().addList(schemeName));
    }
}

