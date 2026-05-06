package com.neobankx.gateway.observability;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationIdWebFilter implements WebFilter, Ordered {
    public static final String HEADER_NAME = "X-Correlation-Id";
    private static final String MDC_KEY = "correlationId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String correlationId = resolveCorrelationId(exchange);
        ServerHttpRequest request = exchange.getRequest().mutate()
                .header(HEADER_NAME, correlationId)
                .build();
        exchange.getResponse().getHeaders().set(HEADER_NAME, correlationId);
        MDC.put(MDC_KEY, correlationId);
        return chain.filter(exchange.mutate().request(request).build())
                .doFinally(signal -> MDC.remove(MDC_KEY));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private String resolveCorrelationId(ServerWebExchange exchange) {
        String provided = exchange.getRequest().getHeaders().getFirst(HEADER_NAME);
        if (provided == null || provided.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return provided.trim();
    }
}

