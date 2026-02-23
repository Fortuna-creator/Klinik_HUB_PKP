package com.hub.app.auth.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
@Slf4j
public class JwtService {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Getter
    @Value("${jwt.expiration-ms}")
    private Long jwtExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(String username, String role) {
        log.debug("Generating JWT token for user: {}", username);
        String token = Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
        log.debug("JWT token generated successfully for user: {}", username);
        return token;
    }

    public String extractUsername(String token) {
        log.debug("Extracting username from JWT token");
        try {
            String username = Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody().getSubject();
            log.debug("Username extracted successfully: {}", username);
            return username;
        } catch (Exception e) {
            log.error("Failed to extract username from JWT token", e);
            throw e;
        }
    }

    public boolean validateToken(String token) {
        log.debug("Validating JWT token");
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            log.debug("JWT token validation successful");
            return true;
        } catch (Exception e) {
            log.warn("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }

}
