package com.enterprise.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Security filter for API key validation and basic authentication
 */
@Component
public class SecurityFilter implements GlobalFilter, Ordered {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String VALID_API_KEY = "enterprise-api-key-2023"; // Em produção, usar configuração externa

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Skip security for health checks and public endpoints
        if (isPublicEndpoint(path)) {
            return chain.filter(exchange);
        }

        String apiKey = exchange.getRequest().getHeaders().getFirst(API_KEY_HEADER);

        if (apiKey == null || !isValidApiKey(apiKey)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/health") ||
               path.startsWith("/actuator") ||
               path.startsWith("/fallback") ||
               path.equals("/");
    }

    private boolean isValidApiKey(String apiKey) {
        // Em produção, implementar validação contra banco de dados ou serviço de autenticação
        return VALID_API_KEY.equals(apiKey);
    }

    @Override
    public int getOrder() {
        return 0; // Execute after logging filter
    }
}
