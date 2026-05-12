package com.gestion.gestion_bus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns("*")
                        // ✅ PATCH + OPTIONS ajoutés — nécessaire pour updateUser et resubmitDossier
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(false);
            }
        };
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/public/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/uploads/public/");

        registry.addResourceHandler("/uploads/private/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/uploads/private/");
    }
}
