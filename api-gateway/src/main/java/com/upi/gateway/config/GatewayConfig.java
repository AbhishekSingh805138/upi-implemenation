package com.upi.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Health check route for gateway itself
                .route("gateway-health", r -> r
                        .path("/health")
                        .filters(f -> f.redirect(301, "/actuator/health"))
                        .uri("no://op")
                )
                
                .build();
    }
}