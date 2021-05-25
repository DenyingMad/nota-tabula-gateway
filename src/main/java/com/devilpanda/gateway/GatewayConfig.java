package com.devilpanda.gateway;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GatewayConfig {
    private final AuthenticationFilter authFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("task-service", p -> p
                        .path("/api/rest/project/**")
                        .filters(f -> f.filter(authFilter))
                        .uri("lb://TASK-SERVICE"))
                .route("auth", p -> p
                        .path("/rest/security/**")
                        .filters(f -> f.filter(authFilter))
                        .uri("lb://AUTH-SERVICE"))
                .route("user-service", p -> p
                        .path("/rest/api/user/**")
                        .filters(f -> f.filter(authFilter))
                        .uri("lb://USER-SERVICE"))
                .build();
    }
}
