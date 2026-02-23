package com.hub.app.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", "mySecretKeyForTestingPurposesOnly123456789");
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 3600000L); // 1 hour
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        String username = "test@example.com";
        String role = "USER";

        String token = jwtService.generateToken(username, role);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        String username = "test@example.com";
        String role = "USER";
        String token = jwtService.generateToken(username, role);

        String extractedUsername = jwtService.extractUsername(token);

        assertEquals(username, extractedUsername);
    }

    @Test
    void validateToken_ShouldReturnTrueForValidToken() {
        String username = "test@example.com";
        String role = "USER";
        String token = jwtService.generateToken(username, role);

        boolean isValid = jwtService.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    void validateToken_ShouldReturnFalseForInvalidToken() {
        String invalidToken = "invalid.token.here";

        boolean isValid = jwtService.validateToken(invalidToken);

        assertFalse(isValid);
    }

    @Test
    void validateToken_ShouldReturnFalseForExpiredToken() {
        // Set very short expiration for testing
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 1L); // 1ms
        String username = "test@example.com";
        String role = "USER";
        String token = jwtService.generateToken(username, role);

        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean isValid = jwtService.validateToken(token);

        assertFalse(isValid);
    }
}