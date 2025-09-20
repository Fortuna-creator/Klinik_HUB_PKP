package com.hub.app.auth.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
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
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
