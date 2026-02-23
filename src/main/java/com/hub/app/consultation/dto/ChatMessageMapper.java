package com.hub.app.consultation.dto;

import com.hub.app.consultation.model.ChatMessage;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageMapper {

    public ChatMessageDTO toDTO(ChatMessage chatMessage) {
        return ChatMessageDTO.builder()
                .id(chatMessage.getId())
                .consultationId(chatMessage.getConsultation().getId())
                .senderId(chatMessage.getSenderId())
                .receiverId(chatMessage.getReceiverId())
                .messageType(chatMessage.getMessageType().name())
                .messageContent(chatMessage.getMessageContent())
                .fileUrl(chatMessage.getFileUrl())
                .isRead(chatMessage.getIsRead())
                .createdAt(chatMessage.getCreatedAt())
                .build();
    }
}