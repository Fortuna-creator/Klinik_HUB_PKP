package com.hub.app.integration.simbg.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(SimbgConfig.class);

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
     * Log konfigurasi setelah properti di-set
     */
    public void logConfiguration() {
        logger.info("SIMBG Configuration loaded - baseUrl: {}, auth token present: {}",
                   baseUrl, auth.getToken() != null && !auth.getToken().isEmpty());
    }

    /**
     * Creates a WebClient bean for SIMBG API communication.
     *
     * @return configured WebClient
     */
    @Bean("simbgWebClient")
    public WebClient simbgWebClient() {
        logger.info("Creating SIMBG WebClient with baseUrl: {}", baseUrl);
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