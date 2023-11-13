package com.example.api.gateway;

import io.jsonwebtoken.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {

	private static final Logger LOG = LoggerFactory.getLogger(AuthorizationHeaderFilter.class);

	private final Environment environment;

	public static class Config {

		private String role;
		private String authority;

		public String getRole() {
			return role;
		}

		public void setRole(String role) {
			this.role = role;
		}

		public String getAuthority() {
			return authority;
		}

		public void setAuthority(String authority) {
			this.authority = authority;
		}
	}

	@Override
	public List<String> shortcutFieldOrder() {
		return Arrays.asList("role", "authority");
	}

	public AuthorizationHeaderFilter(Environment environment) {
		super(Config.class);
		this.environment = environment;
	}

	@Override
	public GatewayFilter apply(Config config) {

		return (exchange, chain) -> {

			ServerHttpRequest request = exchange.getRequest();

			if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
				return onError(exchange, "No authorization header");
			}

			String authorizationHeader = Objects.requireNonNull(request.getHeaders().get(HttpHeaders.AUTHORIZATION))
					.get(0);
			String jwt = authorizationHeader.replace("Bearer", "").trim();

			List<String> authorities = getAuthorities(jwt);

			String role = config.getRole();
			String authority = config.getAuthority();

			return chain.filter(exchange);
		};
	}

	private Mono<Void> onError(ServerWebExchange exchange, String message) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(HttpStatus.UNAUTHORIZED);

		DataBuffer buffer = response.bufferFactory().allocateBuffer(message.length());
		buffer.write(message.getBytes());
		return response.writeAndFlushWith(Flux.just(Flux.just(buffer)));
	}

	private List<String> getAuthorities(String jwt) {

		List<String> returnValue = new ArrayList<>();

		String secretToken = Objects.requireNonNull(environment.getProperty("token.secret"));
		byte[] secretKeyBytes = Base64.getEncoder().encode(secretToken.getBytes());

		String algorithm = Jwts.SIG.HS512.key().build().getAlgorithm();
		SecretKey signingKey = new SecretKeySpec(secretKeyBytes, algorithm);

		JwtParser jwtParser = Jwts.parser()
				.verifyWith(signingKey)
				.build();

		try {
			Jwt<?, ?> jwtObject = jwtParser.parse(jwt);
			Object payload = jwtObject.getPayload();

			if (payload instanceof Claims claims) {
				@SuppressWarnings("unchecked")
				List<Map<String, String>> scopeValues = claims.get("scope", List.class);

				List<String> authorities = scopeValues.stream().map(scopeValue -> scopeValue.get("authority"))
						.collect(Collectors.toList());
				
				returnValue.addAll(authorities);
			}
		} catch (Exception e) {
			LOG.error("JWT parse error. {}", e.getMessage());
		}

		return returnValue;
	}

}
