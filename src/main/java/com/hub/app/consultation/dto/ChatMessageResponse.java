package com.hub.app.consultation.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ChatMessageResponse {
    private UUID id;
    private UUID consultationId;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String messageType;
    private String messageContent;
    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private Boolean isRead;
    private Instant readAt;
    private Instant createdAt;
    private Instant updatedAt;
}