package com.hub.app.consultation.controller;

import com.hub.app.consultation.dto.ChatMessageDTO;
import com.hub.app.consultation.dto.CreateChatMessageRequest;
import com.hub.app.consultation.service.ChatMessageService;
import com.hub.app.consultation.service.ConsultationJwtService;
import com.hub.app.error.ApiErrorBuilder;
import com.hub.app.error.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final ConsultationJwtService jwtService;
    private final ApiErrorBuilder apiErrorBuilder;

    @PostMapping("/{consultationId}/messages")
    @PreAuthorize("hasRole('HOME_OWNER') or hasRole('CONSULTANT')")
    public ResponseEntity<?> sendMessage(
            @PathVariable UUID consultationId,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("messageType") String messageType,
            @RequestParam(value = "messageContent", required = false) String messageContent,
            @RequestParam(value = "voiceFile", required = false) MultipartFile voiceFile) {

        try {
            Long senderId = jwtService.extractUserId(authHeader);

            CreateChatMessageRequest request = new CreateChatMessageRequest();
            request.setMessageType(messageType);
            request.setMessageContent(messageContent);
            request.setVoiceFile(voiceFile);

            ChatMessageDTO response = chatMessageService.sendMessage(consultationId, senderId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        e.getMessage());
        }
    }

    @GetMapping("/{consultationId}/messages")
    @PreAuthorize("hasRole('HOME_OWNER') or hasRole('CONSULTANT')")
    public ResponseEntity<?> getChatMessages(
            @PathVariable UUID consultationId,
            @RequestHeader("Authorization") String authHeader) {

        try {
            Long userId = jwtService.extractUserId(authHeader);
            List<ChatMessageDTO> messages = chatMessageService.getChatMessages(consultationId, userId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        e.getMessage());
        }
    }

    @GetMapping("/{consultationId}/messages/latest")
    @PreAuthorize("hasRole('HOME_OWNER') or hasRole('CONSULTANT')")
    public ResponseEntity<?> getLatestMessage(
            @PathVariable UUID consultationId,
            @RequestHeader("Authorization") String authHeader) {

        try {
            Long userId = jwtService.extractUserId(authHeader);
            ChatMessageDTO latestMessage = chatMessageService.getLatestMessage(consultationId, userId);
            return ResponseEntity.ok(latestMessage);
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        e.getMessage());
        }
    }

    @PatchMapping("/{consultationId}/messages/{messageId}/read")
    @PreAuthorize("hasRole('HOME_OWNER') or hasRole('CONSULTANT')")
    public ResponseEntity<?> markMessageAsRead(
            @PathVariable UUID consultationId,
            @PathVariable UUID messageId,
            @RequestHeader("Authorization") String authHeader) {

        try {
            Long userId = jwtService.extractUserId(authHeader);
            chatMessageService.markAsRead(messageId, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return apiErrorBuilder.error(ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                                        ErrorCode.INTERNAL_ERROR.getCode(),
                                        e.getMessage());
        }
    }
}