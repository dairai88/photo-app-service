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
import java.util.Base64;
import java.util.Objects;

@Component
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {

	private static final Logger LOG = LoggerFactory.getLogger(AuthorizationHeaderFilter.class);

	private final Environment environment;

	public static class Config {

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

			String authorizationHeader = Objects.requireNonNull(request.getHeaders().get(HttpHeaders.AUTHORIZATION)).get(0);
			String jwt = authorizationHeader.replace("Bearer", "").trim();

			LOG.info("jwt token {}, start with blank? {}", jwt, jwt.startsWith(" "));

			if (!isJwtValid(jwt)) {
				return onError(exchange, "JWT token is not valid");
			}

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

	private boolean isJwtValid(String jwt) {
		boolean isValid = true;
		String subject = null;

		String secretToken = Objects.requireNonNull(environment.getProperty("token.secret"));
		byte[] secretKeyBytes = Base64.getEncoder().encode(secretToken.getBytes());
		SecretKey signingKey = new SecretKeySpec(secretKeyBytes, SignatureAlgorithm.HS512.getJcaName());

		JwtParser jwtParser = Jwts.parserBuilder()
				.setSigningKey(signingKey)
				.build();

		try {
			@SuppressWarnings({"unchecked", "rawtypes"})
			Jwt<Header, Claims> parsedToken = jwtParser.parse(jwt);
			subject = parsedToken.getBody().getSubject();
		} catch (Exception e) {
			LOG.error("JWT parse error. {}", e.getMessage());
			isValid = false;
		}

		if (subject == null || subject.isBlank()) {
			isValid = false;
		}

		return isValid;
	}

}
