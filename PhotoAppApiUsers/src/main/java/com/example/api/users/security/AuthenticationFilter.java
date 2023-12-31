package com.example.api.users.security;

import com.example.api.users.service.UsersService;
import com.example.api.users.shared.UserDto;
import com.example.api.users.ui.model.LoginRequestModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;

public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private static final Logger LOG = LoggerFactory.getLogger(AuthenticationFilter.class);

	private final UsersService usersService;
	private final Environment environment;

	public AuthenticationFilter(
			UsersService usersService,
			Environment environment,
			AuthenticationManager authenticationManager) {
		super(authenticationManager);
		this.usersService = usersService;
		this.environment = environment;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {

		try {

			LoginRequestModel credential = new ObjectMapper().readValue(request.getInputStream(), LoginRequestModel.class);

			LOG.info("User {} is trying to log in", credential.getEmail());

			return this.getAuthenticationManager().authenticate(
					new UsernamePasswordAuthenticationToken(
							credential.getEmail(), credential.getPassword(), new ArrayList<>()));
		} catch (IOException e) {

			throw new RuntimeException(e);
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) {

		String username = ((User) authResult.getPrincipal()).getUsername();

		LOG.info("----authenticated user {}.", username);

		UserDto userDetails = usersService.getUserDetailsByEmail(username);

		String tokenSecret = Objects.requireNonNull(environment.getProperty("token.secret"));
		byte[] secretKeyBytes = Base64.getEncoder().encode(tokenSecret.getBytes());

		String algorithm = Jwts.SIG.HS512.key().build().getAlgorithm();
		LOG.info("Algorithm {}.", algorithm);

		SecretKey secretKey = new SecretKeySpec(secretKeyBytes, algorithm);

		Instant now = Instant.now();
		String token = Jwts.builder()
				.claim("scope", authResult.getAuthorities())
				.subject(userDetails.getUserId())
				.expiration(Date.from(now
						.plusMillis(Long.parseLong(Objects.requireNonNull(
								environment.getProperty("token.expiration_time"))))))
				.issuedAt(Date.from(now))
				.signWith(secretKey, Jwts.SIG.HS512)
				.compact();

		response.addHeader("token", token);
		response.addHeader("userId", userDetails.getUserId());
	}

}
