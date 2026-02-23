package com.hub.app.consultation.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ChatMessagePayload {
    private UUID consultationId;
    private Long senderId;
    private Long receiverId;
    private String messageType; // TEXT or VOICE
    private String messageContent;
    private String fileUrl;
}