package com.hub.app.consultation.service;

import com.hub.app.auth.service.JwtService;
import com.hub.app.user.model.User;
import com.hub.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsultationJwtService {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    /**
     * Extract user ID from JWT token for consultation operations
     */
    public Long extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }

        String token = authHeader.replace("Bearer ", "");
        if (!jwtService.validateToken(token)) {
            throw new RuntimeException("Invalid JWT token");
        }

        String email = jwtService.extractUsername(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new com.hub.app.error.DomainException(com.hub.app.error.ErrorCode.USER_NOT_FOUND));

        return user.getId();
    }

    /**
     * Extract user role from JWT token
     */
    public String extractUserRole(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }

        String token = authHeader.replace("Bearer ", "");
        if (!jwtService.validateToken(token)) {
            throw new RuntimeException("Invalid JWT token");
        }

        String email = jwtService.extractUsername(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new com.hub.app.error.DomainException(com.hub.app.error.ErrorCode.USER_NOT_FOUND));

        return user.getRole().name();
    }

    /**
     * Validate JWT token and return user details
     */
    public User validateAndGetUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }

        String token = authHeader.replace("Bearer ", "");
        if (!jwtService.validateToken(token)) {
            throw new RuntimeException("Invalid JWT token");
        }

        String email = jwtService.extractUsername(token);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new com.hub.app.error.DomainException(com.hub.app.error.ErrorCode.USER_NOT_FOUND));
    }

    /**
     * Check if user has specific role
     */
    public boolean hasRole(String authHeader, String requiredRole) {
        String userRole = extractUserRole(authHeader);
        return requiredRole.equals(userRole);
    }

    /**
     * Check if user is HOME_OWNER
     */
    public boolean isHomeOwner(String authHeader) {
        return hasRole(authHeader, "HOME_OWNER");
    }

    /**
     * Check if user is CONSULTANT
     */
    public boolean isConsultant(String authHeader) {
        return hasRole(authHeader, "CONSULTANT");
    }
}