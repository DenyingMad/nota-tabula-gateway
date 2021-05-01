package com.devilpanda.gateway;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RefreshScope
@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GatewayFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationFilter.class);
    private static final String AUTHORIZATION = "Authorization";
    private final RouterValidator routerValidator;
    private final JwtProvider jwtProvider;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (routerValidator.isSecured.test(request)) {
            if (isAuthMissed(request))
                return onError(exchange, "Authorization Header is missing", UNAUTHORIZED);

            String token = getAuthorizationHeader(request);

            boolean isTokenValid = jwtProvider.validateToken(token);
            if (!isTokenValid)
                return onError(exchange, "Authorization Token is invalid", UNAUTHORIZED);

            populateRequestWithHeaders(exchange, token);
        }
        return chain.filter(exchange);
    }

    // =-----------------------------------------------------
    // Implementation
    // =-----------------------------------------------------

    private void populateRequestWithHeaders(ServerWebExchange exchange, String token) {
        Claims claims = jwtProvider.getClaimsFromToken(token);
        exchange.getRequest().mutate()
                .header("userLogin", claims.getSubject())
                .build();
    }

    private String getAuthorizationHeader(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getOrEmpty(AUTHORIZATION).get(0);
        if (authHeader.startsWith("Bearer"))
            return authHeader.replace("Bearer ", "");
        return null;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        LOGGER.warn("Authorization failed. Message -> {}", message);
        return response.setComplete();
    }

    private boolean isAuthMissed(ServerHttpRequest request) {
        return !request.getHeaders().containsKey(AUTHORIZATION);
    }
}
