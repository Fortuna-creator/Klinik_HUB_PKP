package com.hub.app.consultation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hub.app.consultation.dto.ChatMessageDTO;
import com.hub.app.consultation.service.ChatMessageService;
import com.hub.app.consultation.service.ConsultationJwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatMessageController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChatMessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatMessageService chatMessageService;

    @MockitoBean
    private ConsultationJwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void sendMessage_TextMessage_Success() throws Exception {
        // Arrange
        UUID consultationId = UUID.randomUUID();
        Long userId = 1L;

        ChatMessageDTO responseDTO = ChatMessageDTO.builder()
                .id(UUID.randomUUID())
                .consultationId(consultationId)
                .senderId(userId)
                .receiverId(2L)
                .messageType("TEXT")
                .messageContent("Hello World")
                .createdAt(Instant.now())
                .build();

        when(jwtService.extractUserId(anyString())).thenReturn(userId);
        when(chatMessageService.sendMessage(eq(consultationId), eq(userId), any())).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/chats/{consultationId}/messages", consultationId)
                        .param("messageType", "TEXT")
                        .param("messageContent", "Hello World")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageType").value("TEXT"))
                .andExpect(jsonPath("$.messageContent").value("Hello World"));
    }

    @Test
    void sendMessage_VoiceMessage_Success() throws Exception {
        // Arrange
        UUID consultationId = UUID.randomUUID();
        Long userId = 2L;
        MockMultipartFile voiceFile = new MockMultipartFile("voiceFile", "test.mp3", "audio/mpeg", "test data".getBytes());

        ChatMessageDTO responseDTO = ChatMessageDTO.builder()
                .id(UUID.randomUUID())
                .consultationId(consultationId)
                .senderId(userId)
                .receiverId(1L)
                .messageType("VOICE")
                .fileUrl("/hub-files/chat/voice/test.mp3")
                .createdAt(Instant.now())
                .build();

        when(jwtService.extractUserId(anyString())).thenReturn(userId);
        when(chatMessageService.sendMessage(eq(consultationId), eq(userId), any())).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/chats/{consultationId}/messages", consultationId)
                        .file(voiceFile)
                        .param("messageType", "VOICE")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageType").value("VOICE"))
                .andExpect(jsonPath("$.fileUrl").value("/hub-files/chat/voice/test.mp3"));
    }

    @Test
    void getChatMessages_Success() throws Exception {
        // Arrange
        UUID consultationId = UUID.randomUUID();
        Long userId = 1L;

        List<ChatMessageDTO> messages = List.of(
                ChatMessageDTO.builder()
                        .id(UUID.randomUUID())
                        .consultationId(consultationId)
                        .senderId(userId)
                        .receiverId(2L)
                        .messageType("TEXT")
                        .messageContent("Message 1")
                        .createdAt(Instant.now())
                        .build(),
                ChatMessageDTO.builder()
                        .id(UUID.randomUUID())
                        .consultationId(consultationId)
                        .senderId(2L)
                        .receiverId(userId)
                        .messageType("TEXT")
                        .messageContent("Message 2")
                        .createdAt(Instant.now())
                        .build()
        );

        when(jwtService.extractUserId(anyString())).thenReturn(userId);
        when(chatMessageService.getChatMessages(consultationId, userId)).thenReturn(messages);

        // Act & Assert
        mockMvc.perform(get("/api/v1/chats/{consultationId}/messages", consultationId)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].messageContent").value("Message 1"))
                .andExpect(jsonPath("$[1].messageContent").value("Message 2"));
    }

    @Test
    void getLatestMessage_Success() throws Exception {
        // Arrange
        UUID consultationId = UUID.randomUUID();
        Long userId = 2L;

        ChatMessageDTO latestMessage = ChatMessageDTO.builder()
                .id(UUID.randomUUID())
                .consultationId(consultationId)
                .senderId(1L)
                .receiverId(userId)
                .messageType("TEXT")
                .messageContent("Latest message")
                .createdAt(Instant.now())
                .build();

        when(jwtService.extractUserId(anyString())).thenReturn(userId);
        when(chatMessageService.getLatestMessage(consultationId, userId)).thenReturn(latestMessage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/chats/{consultationId}/messages/latest", consultationId)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageContent").value("Latest message"));
    }
}