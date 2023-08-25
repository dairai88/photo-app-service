package com.example.api.users.security;

import java.io.IOException;
import java.util.ArrayList;

import com.example.api.users.service.UsersService;
import com.example.api.users.shared.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.api.users.ui.model.LoginRequestModel;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
			
			LoginRequestModel creds = 
					new ObjectMapper().readValue(request.getInputStream(), LoginRequestModel.class);

			LOG.info("User {} is trying to log in", creds.getEmail());
			
			return this.getAuthenticationManager().authenticate(
					new UsernamePasswordAuthenticationToken(
							creds.getEmail(), creds.getPassword(), new ArrayList<>()));
		} catch (IOException e) {
			
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {

		String username = ((User) authResult.getPrincipal()).getUsername();

		LOG.info("----authenticated user {}.", username);

		UserDto userDetails = usersService.getUserDetailsByEmail(username);

		super.successfulAuthentication(request, response, chain, authResult);
	}

}
