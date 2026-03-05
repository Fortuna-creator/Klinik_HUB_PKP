package com.hub.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration for CORS, message converters, formatters, etc.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configure CORS to allow frontend requests (preflight OPTIONS requests).
     * This enables browser-based clients to make cross-origin requests.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "http://localhost:3000",        // React dev server (local)
                        "http://localhost:5173",        // Vite dev server (local)
                        "http://localhost:5174",        // Vite alternative (local)
                        "https://klinik-hub.vercel.app" // Deployed frontend (example)
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // Cache preflight response for 1 hour

        // Health checks (no restrictions)
        registry.addMapping("/health/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "OPTIONS")
                .maxAge(3600);

        // Actuator endpoints (no restrictions)
        registry.addMapping("/actuator/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "OPTIONS")
                .maxAge(3600);
    }
}
