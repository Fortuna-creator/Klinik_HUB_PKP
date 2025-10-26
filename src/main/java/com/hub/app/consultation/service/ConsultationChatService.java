package com.hub.app.consultation.service;

import com.hub.app.consultation.dto.ChatMessageDTO;
import com.hub.app.consultation.dto.ChatMessageResponse;
import com.hub.app.consultation.dto.CreateChatMessageRequest;
import com.hub.app.consultation.event.ChatMessageCreatedEvent;
import com.hub.app.consultation.model.ChatMessage;
import com.hub.app.consultation.model.Consultation;
import com.hub.app.consultation.model.ConsultationStatus;
import com.hub.app.consultation.repository.ChatMessageRepository;
import com.hub.app.consultation.repository.ConsultationRepository;
import com.hub.app.error.DomainException;
import com.hub.app.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsultationChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ConsultationRepository consultationRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ConsultationHistoryService historyService;

    /**
     * Send chat message during active consultation
     */
    @Transactional
    public ChatMessageResponse sendMessage(UUID consultationId, Long senderId, CreateChatMessageRequest request) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new DomainException(ErrorCode.CONSULTATION_NOT_FOUND));

        // Validate that consultation is in active status
        if (consultation.getStatus() != ConsultationStatus.AKTIF) {
            throw new DomainException(ErrorCode.INVALID_STATUS);
        }

        // Validate that sender is part of this consultation
        if (!consultation.getHomeOwner().getId().equals(senderId) &&
            !consultation.getConsultant().getId().equals(senderId)) {
            throw new DomainException(ErrorCode.UNAUTHORIZED);
        }

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setConsultation(consultation);
        chatMessage.setSenderId(senderId);
        chatMessage.setReceiverId(consultation.getHomeOwner().getId().equals(senderId) ?
            consultation.getConsultant().getId() : consultation.getHomeOwner().getId());
        chatMessage.setMessageContent(request.getMessageContent());
        chatMessage.setMessageType(ChatMessage.MessageType.valueOf(request.getMessageType()));

        // Handle file attachment if provided
        if (request.getFileUrl() != null && !request.getFileUrl().trim().isEmpty()) {
            chatMessage.setFileUrl(request.getFileUrl());
            chatMessage.setFileName(request.getFileName());
            chatMessage.setFileSize(request.getFileSize());
        }

        ChatMessage saved = chatMessageRepository.save(chatMessage);

        // Publish event for real-time notifications
        eventPublisher.publishEvent(new ChatMessageCreatedEvent(this, saved));

        // Create history event for important messages
        if ("TEXT".equals(request.getMessageType()) || "VOICE".equals(request.getMessageType())) {
            String actor = consultation.getHomeOwner().getId().equals(senderId) ? "HOMEOWNER" : "CONSULTANT";
            historyService.createHistoryEvent(
                consultation,
                "CHAT",
                "MESSAGE_SENT",
                "Pesan chat dikirim",
                request.getMessageContent() != null ? request.getMessageContent() : "Pesan suara dikirim",
                java.util.Arrays.asList(),
                java.util.Arrays.asList(),
                actor,
                "MESSAGE",
                "Chat aktif konsultasi"
            );
        }

        return mapToResponse(saved, consultation);
    }

    /**
     * Get chat messages for active consultation
     */
    public List<ChatMessageResponse> getChatMessages(UUID consultationId, Long userId) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new DomainException(ErrorCode.CONSULTATION_NOT_FOUND));

        // Validate that user is part of this consultation
        if (!consultation.getHomeOwner().getId().equals(userId) &&
            !consultation.getConsultant().getId().equals(userId)) {
            throw new DomainException(ErrorCode.UNAUTHORIZED);
        }

        // Only allow chat access for active consultations
        if (consultation.getStatus() != ConsultationStatus.AKTIF) {
            throw new DomainException(ErrorCode.INVALID_STATUS);
        }

        return chatMessageRepository.findByConsultationIdOrderByCreatedAtAsc(consultationId)
                .stream()
                .map(msg -> mapToResponse(msg, consultation))
                .collect(Collectors.toList());
    }

    /**
     * Mark messages as read
     */
    @Transactional
    public void markMessagesAsRead(UUID consultationId, Long userId) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new DomainException(ErrorCode.CONSULTATION_NOT_FOUND));

        // Validate that user is part of this consultation
        if (!consultation.getHomeOwner().getId().equals(userId) &&
            !consultation.getConsultant().getId().equals(userId)) {
            throw new DomainException(ErrorCode.UNAUTHORIZED);
        }

        // Mark all unread messages from other participants as read
        // Note: Repository doesn't have the specific query, so we'll use a simple approach
        List<ChatMessage> allMessages = chatMessageRepository.findByConsultationIdOrderByCreatedAtAsc(consultationId);
        List<ChatMessage> unreadMessages = allMessages.stream()
                .filter(msg -> !msg.getSenderId().equals(userId) && !msg.getIsRead())
                .collect(java.util.stream.Collectors.toList());

        for (ChatMessage message : unreadMessages) {
            message.setIsRead(true);
            message.setReadAt(Instant.now());
        }

        chatMessageRepository.saveAll(unreadMessages);
    }

    /**
     * Get unread message count
     */
    public long getUnreadMessageCount(UUID consultationId, Long userId) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new DomainException(ErrorCode.CONSULTATION_NOT_FOUND));

        // Validate that user is part of this consultation
        if (!consultation.getHomeOwner().getId().equals(userId) &&
            !consultation.getConsultant().getId().equals(userId)) {
            throw new DomainException(ErrorCode.UNAUTHORIZED);
        }

        // Count unread messages from other participants
        return chatMessageRepository.findByConsultationIdOrderByCreatedAtAsc(consultationId)
                .stream()
                .filter(msg -> !msg.getSenderId().equals(userId) && !msg.getIsRead())
                .count();
    }

    /**
     * Check if consultation allows chat
     */
    public boolean canChat(UUID consultationId, Long userId) {
        try {
            Consultation consultation = consultationRepository.findById(consultationId)
                    .orElseThrow(() -> new DomainException(ErrorCode.CONSULTATION_NOT_FOUND));

            // Validate that user is part of this consultation
            if (!consultation.getHomeOwner().getId().equals(userId) &&
                !consultation.getConsultant().getId().equals(userId)) {
                return false;
            }

            // Only active consultations allow chat
            return consultation.getStatus() == ConsultationStatus.AKTIF;
        } catch (Exception e) {
            return false;
        }
    }

    private ChatMessageResponse mapToResponse(ChatMessage chatMessage, Consultation consultation) {
        String senderName;
        if (consultation.getHomeOwner().getId().equals(chatMessage.getSenderId())) {
            senderName = consultation.getHomeOwner().getName();
        } else {
            senderName = consultation.getConsultant().getName();
        }

        return ChatMessageResponse.builder()
                .id(chatMessage.getId())
                .consultationId(chatMessage.getConsultation().getId())
                .senderId(chatMessage.getSenderId())
                .senderName(senderName)
                .receiverId(chatMessage.getReceiverId())
                .messageType(chatMessage.getMessageType().toString())
                .messageContent(chatMessage.getMessageContent())
                .fileUrl(chatMessage.getFileUrl())
                .fileName(chatMessage.getFileName())
                .fileSize(chatMessage.getFileSize())
                .isRead(chatMessage.getIsRead())
                .readAt(chatMessage.getReadAt())
                .createdAt(chatMessage.getCreatedAt())
                .updatedAt(chatMessage.getUpdatedAt())
                .build();
    }
}