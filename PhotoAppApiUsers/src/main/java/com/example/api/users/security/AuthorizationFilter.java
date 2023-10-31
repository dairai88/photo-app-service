package com.example.api.users.security;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthorizationFilter extends BasicAuthenticationFilter {
	
	private static final Logger LOG = LoggerFactory.getLogger(AuthorizationFilter.class);
	
	private final Environment environment;

	public AuthorizationFilter(AuthenticationManager authenticationManager, Environment environment) {
		
		super(authenticationManager);
		
		this.environment = environment;
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		String authorizationHeader = request.getHeader(environment.getProperty("authorization.token.header.name"));
		
		if (authorizationHeader == null || !authorizationHeader.startsWith(environment.getProperty("authorization.token.header.prefix"))) {
			chain.doFilter(request, response);
			return;
		}
		
		UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
		
		SecurityContextHolder.getContext().setAuthentication(authentication);
		chain.doFilter(request, response);
	}

	private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
		
		String authorizationHeader = request.getHeader(environment.getProperty("authorization.token.header.name"));
		
		if (authorizationHeader == null) {
			return null;
		}
		
		String userId = null;
		String token = authorizationHeader.replace("Bearer", "").trim();
		
		String secretToken = Objects.requireNonNull(environment.getProperty("token.secret"));
		byte[] secretKeyBytes = Base64.getEncoder().encode(secretToken.getBytes());
		SecretKey signingKey = new SecretKeySpec(secretKeyBytes, "HmacSHA512");

		JwtParser jwtParser = Jwts.parser()
				.verifyWith(signingKey)
				.build();

		try {
			Jws<Claims> parsedToken = jwtParser.parseSignedClaims(token);
			userId = parsedToken.getPayload().getSubject();
		} catch (Exception e) {
			LOG.error("JWT parse error. {}", e.getMessage());
		}
		
		if (userId == null) {
			return null;
		}
		
		return new UsernamePasswordAuthenticationToken(userId, null, List.of());
	}
	
}
