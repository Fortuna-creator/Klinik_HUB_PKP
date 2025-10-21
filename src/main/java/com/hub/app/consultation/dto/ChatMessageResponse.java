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
    private Long receiverId;
    private String messageType;
    private String messageContent;
    private String fileUrl;
    private Boolean isRead;
    private Instant createdAt;
}