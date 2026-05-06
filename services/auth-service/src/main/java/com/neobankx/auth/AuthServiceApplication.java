package com.neobankx.auth;

import com.neobankx.auth.config.AuthProperties;
import com.neobankx.common.observability.CorrelationIdFilter;
import com.neobankx.common.security.SecurityHeadersFilter;
import com.neobankx.common.web.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackageClasses = AuthProperties.class)
@Import(GlobalExceptionHandler.class)
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }

    @Bean
    CorrelationIdFilter correlationIdFilter() {
        return new CorrelationIdFilter();
    }

    @Bean
    SecurityHeadersFilter securityHeadersFilter() {
        return new SecurityHeadersFilter();
    }
}

