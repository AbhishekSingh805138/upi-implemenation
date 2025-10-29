package com.upi.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class LoggingGlobalFilter implements GlobalFilter, Ordered {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingGlobalFilter.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Log incoming request
        String requestTime = LocalDateTime.now().format(formatter);
        String requestId = generateRequestId();
        
        logger.info("=== INCOMING REQUEST [{}] ===", requestId);
        logger.info("Time: {}", requestTime);
        logger.info("Method: {}", request.getMethod());
        logger.info("URI: {}", request.getURI());
        logger.info("Path: {}", request.getPath());
        logger.info("Query Params: {}", request.getQueryParams());
        logger.info("Headers: {}", request.getHeaders().toSingleValueMap());
        logger.info("Remote Address: {}", request.getRemoteAddress());
        
        // Add request ID to exchange attributes for tracking
        exchange.getAttributes().put("requestId", requestId);
        exchange.getAttributes().put("requestTime", System.currentTimeMillis());
        
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            Long startTime = exchange.getAttribute("requestTime");
            long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;
            
            // Log outgoing response
            logger.info("=== OUTGOING RESPONSE [{}] ===", requestId);
            logger.info("Status Code: {}", response.getStatusCode());
            logger.info("Response Headers: {}", response.getHeaders().toSingleValueMap());
            logger.info("Duration: {} ms", duration);
            logger.info("=== END REQUEST [{}] ===", requestId);
        }));
    }
    
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
    
    private String generateRequestId() {
        return "REQ-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }
}