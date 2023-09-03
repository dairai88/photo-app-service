package com.example.api.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class MyPostFilter implements GlobalFilter {
	
	private final Logger LOG = LoggerFactory.getLogger(MyPostFilter.class);

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		
		return chain.filter(exchange).then(Mono.fromRunnable(() -> {
			
			LOG.info("Global Post-filter executed...");
		}));
	}

}
