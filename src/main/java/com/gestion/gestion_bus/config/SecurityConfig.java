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
							"/uploads/**",            // Fichiers statiques uploadés
							"/types-abonnement/**",
							"/reclamations/all",
							"/reclamations/date/**",
							"/reclamations/status/**",
							"/reclamations/{id}"
						).permitAll()
//                        .requestMatchers("/users/login","/users/register","/users").permitAll()
//                        .requestMatchers("/users/**").permitAll()
//                        .requestMatchers("/lignes/**").permitAll()
//                        .requestMatchers("/uploads/**").permitAll()
//                        .requestMatchers("/bus/**").permitAll()
//                        .requestMatchers("/reclamations/**").permitAll()
//					    .requestMatchers("/api/objets/**").permitAll()
//					    .requestMatchers("/role/**").permitAll()
//					    .requestMatchers("/tickets/**").permitAll()
//					    .requestMatchers("/abonnements/**").permitAll()
//					    .requestMatchers("/types-abonnement/**").permitAll()
//					    .requestMatchers("/admin/**").permitAll()
//					    .requestMatchers("/activities/**").permitAll()
//					    .requestMatchers("/admin/dossiers/**").permitAll()
//					    .requestMatchers("/stripe/webhook").permitAll()
						.requestMatchers(
							"/admin/**",
							"/role/**",
							"/activities/**"
						).hasRole("ADMIN")
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
