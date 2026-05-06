package com.neobankx.gateway.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long started = System.nanoTime();
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getPath().value();
        String routeId = exchange.getAttributeOrDefault("org.springframework.cloud.gateway.support.ServerWebExchangeUtils.gatewayRoute", "unresolved").toString();
        return chain.filter(exchange)
                .doOnSuccess(ignored -> logRequest(exchange, method, path, routeId, started, null))
                .doOnError(error -> logRequest(exchange, method, path, routeId, started, error));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    private void logRequest(ServerWebExchange exchange, String method, String path, String routeId, long started, Throwable error) {
        int status = exchange.getResponse().getStatusCode() == null
                ? 500
                : exchange.getResponse().getStatusCode().value();
        long durationMs = (System.nanoTime() - started) / 1_000_000;
        if (error == null) {
            log.info("gateway_request method={} path={} route={} status={} durationMs={}", method, path, routeId, status, durationMs);
            return;
        }
        log.warn("gateway_request_failed method={} path={} route={} status={} durationMs={} error={}",
                method, path, routeId, status, durationMs, error.getClass().getSimpleName());
    }
}

