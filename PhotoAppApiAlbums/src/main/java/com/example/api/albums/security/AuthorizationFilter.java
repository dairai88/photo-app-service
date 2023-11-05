package com.example.api.albums.security;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.example.jwt.JwtClaminsParser;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthorizationFilter extends BasicAuthenticationFilter {

	private final Environment environment;

	public AuthorizationFilter(AuthenticationManager authenticationManager, Environment environment) {
		
		super(authenticationManager);
		
		this.environment = environment;
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		String authorizationHeader = request.getHeader(environment.getProperty("authorization.token.header.name"));
		String authorizationTokenPrefix = environment.getProperty("authorization.token.header.prefix");

		if (authorizationHeader == null
				|| !authorizationHeader.startsWith(
						authorizationTokenPrefix == null ? "Bearer" : authorizationTokenPrefix)) {
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
		
		String userId;
		String token = authorizationHeader.replace("Bearer", "").trim();
		
		String secretToken = Objects.requireNonNull(environment.getProperty("token.secret"));

		JwtClaminsParser jwtClaminsParser = new JwtClaminsParser(token, secretToken);
		userId = jwtClaminsParser.getJwtSubject();
		Collection<GrantedAuthority> authorities = jwtClaminsParser.getUserAuthorities();

		if (userId == null) {
			return null;
		}
		
		return new UsernamePasswordAuthenticationToken(userId, null, authorities);
	}

}
