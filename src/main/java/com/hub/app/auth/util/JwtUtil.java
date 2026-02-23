package com.hub.app.auth.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {
    private final Key key;
    private final long expirationMs;
    private final Clock clock;

    // Optional sanity check to guard very short secrets for HS256
    @PostConstruct
    void ensureKeyStrength() {
        // HS256 requires a key of sufficient length; in production ensure a strong secret
        // This is a no-op placeholder; consider asserting length >= 32 bytes, etc.
    }

    public JwtUtil(Key key, @Value("${jwt.expiration-ms:3600000}") long expirationMs, Clock clock) {
        this.key = key;
        this.expirationMs = expirationMs;
        this.clock = clock;
    }

    public String generateToken(String username) {
        Instant now = Instant.now(clock);
        Instant exp = now.plusMillis(expirationMs);
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        Claims claims = parseClaims(token);
        Date exp = claims.getExpiration();
        return exp != null && exp.after(Date.from(Instant.now(clock)));
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
