package com.hub.app.auth.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Utility class for managing HttpOnly authentication cookies.
 * Sets secure JWT tokens in HTTP-only cookies to prevent XSS attacks.
 */
@Component
public class CookieUtil {

    private static final String AUTH_TOKEN_NAME = "auth_token";
    private static final String COOKIE_PATH = "/api";
    private static final long COOKIE_MAX_AGE_SECONDS = 604800; // 7 days

    /**
     * Set HttpOnly authentication cookie with JWT token.
     * 
     * @param response HttpServletResponse to set the cookie
     * @param token JWT token value
     */
    public void setAuthCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie
                .from(AUTH_TOKEN_NAME, token)
                .httpOnly(true)              // Prevent JavaScript access (XSS protection)
                .secure(true)                // HTTPS only
                .path(COOKIE_PATH)           // Only send to /api paths
                .maxAge(COOKIE_MAX_AGE_SECONDS)  // 7 days
                .sameSite("Strict")          // CSRF protection
                .build();
        
        response.addHeader("Set-Cookie", cookie.toString());
    }

    /**
     * Clear the authentication cookie (logout).
     * 
     * @param response HttpServletResponse to clear the cookie
     */
    public void clearAuthCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie
                .from(AUTH_TOKEN_NAME, "")
                .httpOnly(true)
                .secure(true)
                .path(COOKIE_PATH)
                .maxAge(0)                   // Expire immediately
                .sameSite("Strict")
                .build();
        
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
