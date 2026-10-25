package com.gestion.gestion_bus.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthFilter;
	private final AuthenticationProvider authProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
					.requestMatchers(
							"/api/health",
							"/users/login",
						"/users/register",
						"/users/forgot-password",
						"/users/verify-reset-code",
						"/users/reset-password",
						"/lignes/**",
						"/bus/**",
						"/horaires/**",
						"/reclamations/**",
						"/api/objets/**",
						"/stripe/webhook",
						"/tickets/**",
						"/uploads/public/**",
						"/types-abonnement/**",
						"/reclamations/all",
						"/reclamations/date/**",
						"/reclamations/status/**",
						"/reclamations/{id}"
					).permitAll()
					.requestMatchers(
						"/admin/**",
						"/role/**"
					).hasRole("ADMIN")
						.requestMatchers("/uploads/private/**").authenticated()
						.requestMatchers("/cards/**").authenticated()
						.anyRequest().authenticated()
                )

				.sessionManagement(session ->
						session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				)
				.authenticationProvider(authProvider)
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
