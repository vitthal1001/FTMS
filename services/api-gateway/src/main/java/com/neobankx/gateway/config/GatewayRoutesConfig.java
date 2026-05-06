package com.neobankx.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayRoutesConfig {
    @Bean
    RouteLocator neobankxRoutes(
            RouteLocatorBuilder routes,
            RedisRateLimiter redisRateLimiter,
            KeyResolver clientKeyResolver,
            @Value("${neobankx.routes.auth-service-uri:http://localhost:8081}") String authServiceUri,
            @Value("${neobankx.routes.account-service-uri:http://localhost:8082}") String accountServiceUri
    ) {
        return routes.routes()
                .route("auth-service", route -> route
                        .path("/api/v1/auth/**")
                        .filters(filters -> filters
                                .requestRateLimiter(config -> {
                                    config.setRateLimiter(redisRateLimiter);
                                    config.setKeyResolver(clientKeyResolver);
                                })
                                .circuitBreaker(config -> config
                                        .setName("auth-service")
                                        .setFallbackUri("forward:/fallback/auth-service")))
                        .uri(authServiceUri))
                .route("account-service", route -> route
                        .path("/api/v1/accounts/**")
                        .filters(filters -> filters
                                .requestRateLimiter(config -> {
                                    config.setRateLimiter(redisRateLimiter);
                                    config.setKeyResolver(clientKeyResolver);
                                })
                                .circuitBreaker(config -> config
                                        .setName("account-service")
                                        .setFallbackUri("forward:/fallback/account-service")))
                        .uri(accountServiceUri))
                .build();
    }

    @Bean
    RedisRateLimiter redisRateLimiter(
            @Value("${neobankx.rate-limit.replenish-rate:20}") int replenishRate,
            @Value("${neobankx.rate-limit.burst-capacity:40}") int burstCapacity,
            @Value("${neobankx.rate-limit.requested-tokens:1}") int requestedTokens
    ) {
        return new RedisRateLimiter(replenishRate, burstCapacity, requestedTokens);
    }

    @Bean
    KeyResolver clientKeyResolver() {
        return exchange -> {
            String user = exchange.getRequest().getHeaders().getFirst("X-Authenticated-Subject");
            String clientId = exchange.getRequest().getHeaders().getFirst("X-Client-Id");
            String remoteAddress = exchange.getRequest().getRemoteAddress() == null
                    ? "unknown"
                    : exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            return Mono.just(firstNonBlank(user, clientId, remoteAddress));
        };
    }

    private static String firstNonBlank(String first, String second, String fallback) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return fallback;
    }
}
