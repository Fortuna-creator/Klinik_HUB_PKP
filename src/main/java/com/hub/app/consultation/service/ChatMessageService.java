package com.hub.app.consultation.service;

import com.hub.app.consultation.dto.ChatMessageDTO;
import com.hub.app.consultation.dto.ChatMessageMapper;
import com.hub.app.consultation.dto.CreateChatMessageRequest;
import com.hub.app.consultation.event.ChatMessageCreatedEvent;
import com.hub.app.consultation.model.ChatMessage;
import com.hub.app.consultation.model.Consultation;
import com.hub.app.consultation.model.ConsultationStatus;
import com.hub.app.consultation.repository.ChatMessageRepository;
import com.hub.app.consultation.repository.ConsultationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for chat message operations using Spring's in-memory WebSocket broker.
 * No external message broker required - all real-time communication handled by Spring WebSocket.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ConsultationRepository consultationRepository;
    private final ChatStorageService chatStorageService;
    private final ChatMessageMapper chatMessageMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ChatMessageDTO sendMessage(UUID consultationId, Long senderId, CreateChatMessageRequest request) {
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

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setConsultation(consultation);
        chatMessage.setSenderId(senderId);
        chatMessage.setReceiverId(receiverId);
        chatMessage.setMessageType(ChatMessage.MessageType.valueOf(request.getMessageType().toUpperCase()));

        if (chatMessage.getMessageType() == ChatMessage.MessageType.TEXT) {
            if (request.getMessageContent() == null || request.getMessageContent().trim().isEmpty()) {
                throw new RuntimeException("Message content is required for text messages");
            }
            chatMessage.setMessageContent(request.getMessageContent());
        } else if (chatMessage.getMessageType() == ChatMessage.MessageType.VOICE) {
            if (request.getVoiceFile() == null || request.getVoiceFile().isEmpty()) {
                throw new RuntimeException("Voice file is required for voice messages");
            }
            String fileUrl = chatStorageService.uploadVoiceNote(request.getVoiceFile());
            chatMessage.setFileUrl(fileUrl);
        }

        chatMessage = chatMessageRepository.save(chatMessage);

        // Publish event for notifications
        eventPublisher.publishEvent(new ChatMessageCreatedEvent(this, chatMessage));

        return chatMessageMapper.toDTO(chatMessage);
    }

    public List<ChatMessageDTO> getChatMessages(UUID consultationId, Long userId) {
        // Validate user has access to this consultation
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new RuntimeException("Consultation not found"));

        if (!consultation.getHomeOwner().getId().equals(userId) &&
            !consultation.getConsultant().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized to access this chat");
        }

        List<ChatMessage> messages = chatMessageRepository.findByConsultationIdOrderByCreatedAtAsc(consultationId);
        return messages.stream()
                .map(chatMessageMapper::toDTO)
                .collect(Collectors.toList());
    }

    public ChatMessageDTO getLatestMessage(UUID consultationId, Long userId) {
        // Validate user has access to this consultation
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new RuntimeException("Consultation not found"));

        if (!consultation.getHomeOwner().getId().equals(userId) &&
            !consultation.getConsultant().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized to access this chat");
        }

        ChatMessage latestMessage = chatMessageRepository.findLatestByConsultationId(consultationId);
        return latestMessage != null ? chatMessageMapper.toDTO(latestMessage) : null;
    }

    @Transactional
    public void markAsRead(UUID messageId, Long userId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        // Validate user can mark this message as read
        if (!message.getReceiverId().equals(userId)) {
            throw new RuntimeException("Unauthorized to mark this message as read");
        }

        message.setIsRead(true);
        chatMessageRepository.save(message);
    }
}