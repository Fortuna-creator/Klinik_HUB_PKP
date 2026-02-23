package com.hub.app.consultation.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time chat using Spring's built-in simple broker.
 * This configuration uses in-memory message broker without external dependencies.
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple in-memory message broker for real-time communication
        // No external message broker (RabbitMQ, Kafka, Redis) required
        config.enableSimpleBroker("/topic", "/queue");

        // Set application destination prefix for client-to-server messages
        config.setApplicationDestinationPrefixes("/app");

        // Set user destination prefix for private messages
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register WebSocket endpoint with SockJS fallback for browser compatibility
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*") // Allow CORS for frontend
                .withSockJS();
    }
}