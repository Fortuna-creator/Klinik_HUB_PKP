package com.hub.app.consultation.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Event listener for chat message notifications using Spring's in-memory broker.
 * No external message broker required - uses SimpMessagingTemplate directly.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatNotificationListener {

    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleChatMessageCreated(ChatMessageCreatedEvent event) {
        var chatMessage = event.getChatMessage();

        // Send notification to receiver via WebSocket using in-memory broker
        String notificationDestination = "/queue/notifications/" + chatMessage.getReceiverId();
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "NEW_MESSAGE");
        notification.put("consultationId", chatMessage.getConsultation().getId());
        notification.put("senderId", chatMessage.getSenderId());
        notification.put("messageType", chatMessage.getMessageType().name());
        notification.put("createdAt", chatMessage.getCreatedAt());

        messagingTemplate.convertAndSend(notificationDestination, notification);

        log.info("Sent chat notification to user {} for consultation {} using in-memory broker",
                chatMessage.getReceiverId(), chatMessage.getConsultation().getId());
    }
}