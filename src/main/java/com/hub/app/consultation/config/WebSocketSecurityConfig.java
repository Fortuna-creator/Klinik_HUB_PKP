package com.hub.app.consultation.config;

import com.hub.app.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

/**
 * WebSocket security configuration for JWT authentication in STOMP connections.
 * This interceptor validates JWT tokens during WebSocket handshake and sets user authentication.
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@RequiredArgsConstructor
@Slf4j
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtService jwtService;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Extract JWT token from STOMP CONNECT headers
                    List<String> authorization = accessor.getNativeHeader("Authorization");
                    if (authorization != null && !authorization.isEmpty()) {
                        String token = authorization.get(0);
                        if (token.startsWith("Bearer ")) {
                            token = token.substring(7);
                        }

                        try {
                            if (jwtService.validateToken(token)) {
                                String username = jwtService.extractUsername(token);
                                // Create authentication with user ID as principal
                                Authentication auth = new UsernamePasswordAuthenticationToken(
                                        username, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
                                SecurityContextHolder.getContext().setAuthentication(auth);
                                accessor.setUser(auth);

                                log.debug("WebSocket authentication successful for user: {}", username);
                            } else {
                                log.warn("Invalid JWT token provided for WebSocket connection");
                            }
                        } catch (Exception e) {
                            log.error("Error validating JWT token for WebSocket: {}", e.getMessage());
                        }
                    } else {
                        log.warn("No Authorization header provided for WebSocket connection");
                    }
                }

                return message;
            }
        });
    }
}