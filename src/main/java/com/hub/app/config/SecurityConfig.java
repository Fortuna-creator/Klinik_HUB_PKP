package com.hub.app.config;

import com.hub.app.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain security(HttpSecurity http,
                                        JwtAuthenticationFilter jwtAuthenticationFilter,
                                        AuthenticationEntryPoint authenticationEntryPoint,
                                        CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))  // ✅ Apply CORS at security level
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        // ── CORS Preflight: Allow OPTIONS requests without auth ──
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ── Public endpoints (no auth) ──────────────────────
                        .requestMatchers("/api/auth/**", "/health", "/actuator/**").permitAll()
                        .requestMatchers("/api/offline/balai/**").permitAll()

                        // Balai list is public per contract
                        .requestMatchers(HttpMethod.GET, "/api/balai/list").permitAll()

                        // ── Warga (citizen / HOMEOWNER) endpoints ───────────
                        .requestMatchers(HttpMethod.POST, "/api/consultations/book").hasRole("HOMEOWNER")
                        .requestMatchers(HttpMethod.GET, "/api/consultations/me").hasRole("HOMEOWNER")
                        .requestMatchers(HttpMethod.POST, "/api/consultations/*/verify").hasRole("HOMEOWNER")
                        .requestMatchers(HttpMethod.GET, "/api/confirmations/inbox").hasRole("HOMEOWNER")
                        .requestMatchers("/api/users/consultants/completed").hasRole("HOMEOWNER")

                        // ── Balai-only dashboard endpoints ──────────────────
                        .requestMatchers("/api/dashboard/**").hasRole("BALAI")
                        .requestMatchers(HttpMethod.PUT, "/api/consultations/*/confirm").hasRole("BALAI")
                        .requestMatchers(HttpMethod.PUT, "/api/consultations/*/reject").hasRole("BALAI")
                        .requestMatchers(HttpMethod.PUT, "/api/consultations/*/complete").hasRole("BALAI")

                        // Available-slots requires any authenticated user
                        .requestMatchers(HttpMethod.GET, "/api/balai/*/available-slots").authenticated()

                        // Remaining balai admin endpoints require BALAI role
                        .requestMatchers("/api/balai/**").hasRole("BALAI")

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        return new JwtAuthenticationFilter(jwtService, userDetailsService);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",        // React dev server
                "http://localhost:5173",        // Vite dev server
                "http://localhost:5174",        // Vite alternative
                "https://klinik-hub.vercel.app" // Production frontend
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);  // ✅ Required for HttpOnly cookies
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);  // Apply to all paths
        return source;
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(401);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Unauthorized\"}");
        };
    }
}