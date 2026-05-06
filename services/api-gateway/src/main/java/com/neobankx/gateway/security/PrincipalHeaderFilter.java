package com.neobankx.gateway.security;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class PrincipalHeaderFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
                .cast(JwtAuthenticationToken.class)
                .map(authentication -> exchange.mutate()
                        .request(exchange.getRequest().mutate()
                                .headers(headers -> {
                                    headers.remove("X-Authenticated-Subject");
                                    headers.remove("X-Authenticated-Roles");
                                    headers.add("X-Authenticated-Subject", authentication.getToken().getSubject());
                                    headers.add("X-Authenticated-Roles", String.join(",", authentication.getAuthorities().stream()
                                            .map(Object::toString)
                                            .toList()));
                                })
                                .build())
                        .build())
                .defaultIfEmpty(exchange)
                .flatMap(chain::filter);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 20;
    }
}

