package com.example.api.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class MyPreFilter implements GlobalFilter {

    private final static Logger LOG = LoggerFactory.getLogger(MyPreFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        LOG.info("My first Pre-filter is executed...");

        ServerHttpRequest request = exchange.getRequest();
        String requestPath = request.getPath().toString();
        LOG.info("Request path = {}", requestPath);

        HttpHeaders headers = exchange.getRequest().getHeaders();

        headers.forEach((key, value) -> LOG.info("{} = {}", key, headers.getFirst(key)));
        
        return chain.filter(exchange);
    }
}
