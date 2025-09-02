package com.enterprise.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Gateway configuration for CORS and additional routing
 */
@Configuration
public class GatewayConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOriginPatterns(Arrays.asList("*"));
        corsConfig.setMaxAge(3600L);
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfig.setAllowedHeaders(Arrays.asList("*"));
        corsConfig.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }

    @Bean
    public RouteLocator additionalRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // API Documentation Routes
            .route("swagger-inventory", r -> r
                .path("/swagger/inventory/**")
                .filters(f -> f.stripPrefix(2))
                .uri("${services.inventory.url:http://localhost:8080}"))

            .route("swagger-store", r -> r
                .path("/swagger/store/**")
                .filters(f -> f.stripPrefix(2))
                .uri("${services.store.url:http://localhost:8081}"))

            .route("swagger-notification", r -> r
                .path("/swagger/notification/**")
                .filters(f -> f.stripPrefix(2))
                .uri("${services.notification.url:http://localhost:8082}"))

            // Aggregated API documentation
            .route("api-docs", r -> r
                .path("/v3/api-docs/**")
                .uri("${services.inventory.url:http://localhost:8080}"))

            .build();
    }
}
