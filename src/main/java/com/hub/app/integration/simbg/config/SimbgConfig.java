package com.hub.app.integration.simbg.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration class for SIMBG integration.
 */
@Configuration
@ConfigurationProperties(prefix = "simbg")
public class SimbgConfig {

    private String baseUrl;
    private Auth auth = new Auth();

    /**
     * Inner class for authentication configuration.
     */
    public static class Auth {
        private String token;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

    /**
     * Creates a WebClient bean for SIMBG API communication.
     *
     * @return configured WebClient
     */
    @Bean("simbgWebClient")
    public WebClient simbgWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + auth.getToken())
                .build();
    }

    // Getters and Setters
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Auth getAuth() {
        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }
}