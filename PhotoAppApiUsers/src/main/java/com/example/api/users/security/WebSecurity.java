package com.example.api.users.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import com.example.api.users.service.UsersService;

@Configuration
@EnableWebSecurity
public class WebSecurity {

	private final Environment env;
	private final UsersService usersService;
	private final BCryptPasswordEncoder bCryptPasswordEncoder;

	public WebSecurity(Environment env,
			UsersService usersService,
			BCryptPasswordEncoder bCryptPasswordEncoder) {
		this.env = env;
		this.usersService = usersService;
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
	}

	@Bean
	MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector) {
		return new MvcRequestMatcher.Builder(introspector);
	}

	@Bean
	SecurityFilterChain configure(HttpSecurity http, MvcRequestMatcher.Builder mvc) throws Exception {

		// Configure AuthenticationManagerBuilder
		AuthenticationManagerBuilder authenticationManagerBuilder = http
				.getSharedObject(AuthenticationManagerBuilder.class);
		authenticationManagerBuilder.userDetailsService(usersService).passwordEncoder(bCryptPasswordEncoder);
		AuthenticationManager authenticationManager = authenticationManagerBuilder.build();

		// Create AuthenticationFilter
		AuthenticationFilter authenticationFilter =
				new AuthenticationFilter(usersService, env, authenticationManager);
		authenticationFilter.setFilterProcessesUrl(env.getProperty("login.url.path"));

		http.csrf(AbstractHttpConfigurer::disable);

		http.authorizeHttpRequests(authorizeHttpRequests -> {

			authorizeHttpRequests.requestMatchers(
					mvc.pattern(HttpMethod.GET, "/users/status/check"),
					mvc.pattern(HttpMethod.POST, "/users"))
					.access(
							new WebExpressionAuthorizationManager(
									"hasIpAddress('" + env.getProperty("gateway.ip") + "')"));

			authorizeHttpRequests.requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll();
		});

		http.addFilter(authenticationFilter).authenticationManager(authenticationManager);

		http.sessionManagement(
				sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));

		return http.build();
	}
}
