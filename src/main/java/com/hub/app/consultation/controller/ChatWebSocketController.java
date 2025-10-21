package com.hub.app.consultation.controller;

import com.hub.app.consultation.dto.ChatMessagePayload;
import com.hub.app.consultation.dto.ChatMessageResponse;
import com.hub.app.consultation.event.ChatMessageCreatedEvent;
import com.hub.app.consultation.model.ChatMessage;
import com.hub.app.consultation.repository.ChatMessageRepository;
import com.hub.app.consultation.repository.ConsultationRepository;
import com.hub.app.consultation.model.Consultation;
import com.hub.app.consultation.model.ConsultationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * WebSocket controller for real-time chat using STOMP protocol.
 * Uses Spring's built-in simple broker for in-memory message routing.
 * No external message broker required.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final ChatMessageRepository chatMessageRepository;
    private final ConsultationRepository consultationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ApplicationEventPublisher eventPublisher;

    @MessageMapping("/chat/{consultationId}/send")
    public void sendMessage(@DestinationVariable UUID consultationId,
                           @Payload ChatMessagePayload payload,
                           Authentication authentication) {

        try {
            // Extract user ID from authentication (set by WebSocketSecurityConfig)
            Long senderId = Long.valueOf(authentication.getName());

            // Validate consultation exists and is approved
            Consultation consultation = consultationRepository.findById(consultationId)
                    .orElseThrow(() -> new RuntimeException("Consultation not found"));

            if (consultation.getStatus() != ConsultationStatus.AKTIF) {
                throw new RuntimeException("Chat is only allowed for approved consultations");
            }

            // Validate sender is part of the consultation
            if (!consultation.getHomeOwner().getId().equals(senderId) &&
                !consultation.getConsultant().getId().equals(senderId)) {
                throw new RuntimeException("Unauthorized to send message in this consultation");
            }

            // Determine receiver
            Long receiverId = consultation.getHomeOwner().getId().equals(senderId) ?
                    consultation.getConsultant().getId() : consultation.getHomeOwner().getId();

            // Create and save message
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setConsultation(consultation);
            chatMessage.setSenderId(senderId);
            chatMessage.setReceiverId(receiverId);
            chatMessage.setMessageType(ChatMessage.MessageType.valueOf(payload.getMessageType().toUpperCase()));
            chatMessage.setMessageContent(payload.getMessageContent());
            chatMessage.setFileUrl(payload.getFileUrl());
            chatMessage.setIsRead(false);

            chatMessage = chatMessageRepository.save(chatMessage);

            // Create response
            ChatMessageResponse response = ChatMessageResponse.builder()
                    .id(chatMessage.getId())
                    .consultationId(consultationId)
                    .senderId(senderId)
                    .receiverId(receiverId)
                    .messageType(payload.getMessageType())
                    .messageContent(payload.getMessageContent())
                    .fileUrl(payload.getFileUrl())
                    .isRead(false)
                    .createdAt(chatMessage.getCreatedAt())
                    .build();

            // Broadcast to chat room
            String chatDestination = "/topic/chat/" + consultationId;
            messagingTemplate.convertAndSend(chatDestination, response);

            // Publish event for notifications
            eventPublisher.publishEvent(new ChatMessageCreatedEvent(this, chatMessage));

            log.info("Message sent in consultation {} from user {} to user {}",
                    consultationId, senderId, receiverId);

        } catch (Exception e) {
            log.error("Error sending WebSocket message: {}", e.getMessage());
            // In a real application, you might want to send an error message back to the sender
        }
    }

    @MessageMapping("/chat/{consultationId}/read/{messageId}")
    public void markAsRead(@DestinationVariable UUID consultationId,
                          @DestinationVariable UUID messageId,
                          Authentication authentication) {

        try {
            Long userId = Long.valueOf(authentication.getName());

            // Find and update message
            ChatMessage message = chatMessageRepository.findById(messageId)
                    .orElseThrow(() -> new RuntimeException("Message not found"));

            // Validate user can mark this message as read
            if (!message.getReceiverId().equals(userId)) {
                throw new RuntimeException("Unauthorized to mark this message as read");
            }

            message.setIsRead(true);
            chatMessageRepository.save(message);

            // Broadcast read status to chat room
            String readDestination = "/topic/chat/" + consultationId + "/read";
            Map<String, Object> readStatus = new HashMap<>();
            readStatus.put("messageId", messageId);
            readStatus.put("isRead", true);
            readStatus.put("readBy", userId);

            messagingTemplate.convertAndSend(readDestination, readStatus);

            log.info("Message {} marked as read by user {}", messageId, userId);

        } catch (Exception e) {
            log.error("Error marking message as read: {}", e.getMessage());
        }
    }
}