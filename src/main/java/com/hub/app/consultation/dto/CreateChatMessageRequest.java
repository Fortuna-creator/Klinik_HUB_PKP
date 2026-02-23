package com.hub.app.consultation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreateChatMessageRequest {

    @NotNull(message = "Message type is required")
    private String messageType;

    private String messageContent;

    private MultipartFile voiceFile;

    // File attachment fields
    private String fileUrl;
    private String fileName;
    private Long fileSize;
}