package com.hub.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Clock;

import io.jsonwebtoken.security.Keys;

@Configuration
public class JwtConfig {
    @Bean
    public Key jwtSigningKey(@Value("${jwt.secret:change-me}") String secret) {
        // derive HMAC key from secret bytes (for production use proper key management)
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Bean
    public Clock systemClock() {
        return Clock.systemUTC();
    }
}
