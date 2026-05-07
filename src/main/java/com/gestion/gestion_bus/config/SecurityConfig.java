package com.gestion.gestion_bus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
	@Bean
	public PasswordEncoder passwordEncoder(){
		return new BCryptPasswordEncoder();
	}


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/users/login","/users/register","/users").permitAll()
                        .requestMatchers("/users/**").permitAll()
                        .requestMatchers("/lignes/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/bus/**").permitAll()
                        .requestMatchers("/reclamations/**").permitAll()
					    .requestMatchers("/api/objets/**").permitAll()
					    .requestMatchers("/role/**").permitAll()
					    .requestMatchers("/tickets/**").permitAll()
					    .requestMatchers("/abonnements/**").permitAll()
					    .requestMatchers("/types-abonnement/**").permitAll()
					    .requestMatchers("/admin/**").permitAll()
					    .requestMatchers("/admin/dossiers/**").permitAll()
					    .requestMatchers("/stripe/webhook").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }



}
