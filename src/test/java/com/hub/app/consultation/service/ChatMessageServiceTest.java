package com.hub.app.consultation.service;

import com.hub.app.consultation.dto.ChatMessageDTO;
import com.hub.app.consultation.dto.ChatMessageMapper;
import com.hub.app.consultation.dto.CreateChatMessageRequest;
import com.hub.app.consultation.model.ChatMessage;
import com.hub.app.consultation.model.Consultation;
import com.hub.app.consultation.model.ConsultationStatus;
import com.hub.app.consultation.repository.ChatMessageRepository;
import com.hub.app.consultation.repository.ConsultationRepository;
import com.hub.app.user.model.Consultant;
import com.hub.app.user.model.HomeOwner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ConsultationRepository consultationRepository;

    @Mock
    private ChatStorageService chatStorageService;

    @Mock
    private ChatMessageMapper chatMessageMapper;

    @InjectMocks
    private ChatMessageService chatMessageService;

    private Consultation consultation;
    private HomeOwner homeOwner;
    private Consultant consultant;
    private UUID consultationId;
    private Long homeOwnerId;
    private Long consultantId;

    @BeforeEach
    void setUp() {
        consultationId = UUID.randomUUID();
        homeOwnerId = 1L;
        consultantId = 2L;

        homeOwner = new HomeOwner();
        homeOwner.setId(homeOwnerId);
        homeOwner.setName("Test Home Owner");

        consultant = new Consultant();
        consultant.setId(consultantId);
        consultant.setName("Test Consultant");

        consultation = new Consultation();
        consultation.setId(consultationId);
        consultation.setHomeOwner(homeOwner);
        consultation.setConsultant(consultant);
        consultation.setStatus(ConsultationStatus.AKTIF);
    }

    @Test
    void sendMessage_TextMessage_Success() {
        // Arrange
        CreateChatMessageRequest request = new CreateChatMessageRequest();
        request.setMessageType("TEXT");
        request.setMessageContent("Hello, this is a test message");

        ChatMessage savedMessage = new ChatMessage();
        savedMessage.setId(UUID.randomUUID());
        savedMessage.setConsultation(consultation);
        savedMessage.setSenderId(homeOwnerId);
        savedMessage.setReceiverId(consultantId);
        savedMessage.setMessageType(ChatMessage.MessageType.TEXT);
        savedMessage.setMessageContent("Hello, this is a test message");
        savedMessage.setCreatedAt(Instant.now());

        ChatMessageDTO expectedDTO = ChatMessageDTO.builder()
                .id(savedMessage.getId())
                .consultationId(consultationId)
                .senderId(homeOwnerId)
                .receiverId(consultantId)
                .messageType("TEXT")
                .messageContent("Hello, this is a test message")
                .createdAt(savedMessage.getCreatedAt())
                .build();

        when(consultationRepository.findById(consultationId)).thenReturn(Optional.of(consultation));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(savedMessage);
        when(chatMessageMapper.toDTO(savedMessage)).thenReturn(expectedDTO);

        // Act
        ChatMessageDTO result = chatMessageService.sendMessage(consultationId, homeOwnerId, request);

        // Assert
        assertNotNull(result);
        assertEquals("TEXT", result.getMessageType());
        assertEquals("Hello, this is a test message", result.getMessageContent());
        verify(chatMessageRepository).save(any(ChatMessage.class));
        verify(chatStorageService, never()).uploadVoiceNote(any());
    }

    @Test
    void sendMessage_VoiceMessage_Success() {
        // Arrange
        MockMultipartFile voiceFile = new MockMultipartFile("voice", "test.mp3", "audio/mpeg", "test data".getBytes());
        CreateChatMessageRequest request = new CreateChatMessageRequest();
        request.setMessageType("VOICE");
        request.setVoiceFile(voiceFile);

        String fileUrl = "/hub-files/chat/voice/test.mp3";

        ChatMessage savedMessage = new ChatMessage();
        savedMessage.setId(UUID.randomUUID());
        savedMessage.setConsultation(consultation);
        savedMessage.setSenderId(homeOwnerId);
        savedMessage.setReceiverId(consultantId);
        savedMessage.setMessageType(ChatMessage.MessageType.VOICE);
        savedMessage.setFileUrl(fileUrl);
        savedMessage.setCreatedAt(Instant.now());

        ChatMessageDTO expectedDTO = ChatMessageDTO.builder()
                .id(savedMessage.getId())
                .consultationId(consultationId)
                .senderId(homeOwnerId)
                .receiverId(consultantId)
                .messageType("VOICE")
                .fileUrl(fileUrl)
                .createdAt(savedMessage.getCreatedAt())
                .build();

        when(consultationRepository.findById(consultationId)).thenReturn(Optional.of(consultation));
        when(chatStorageService.uploadVoiceNote(voiceFile)).thenReturn(fileUrl);
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(savedMessage);
        when(chatMessageMapper.toDTO(savedMessage)).thenReturn(expectedDTO);

        // Act
        ChatMessageDTO result = chatMessageService.sendMessage(consultationId, homeOwnerId, request);

        // Assert
        assertNotNull(result);
        assertEquals("VOICE", result.getMessageType());
        assertEquals(fileUrl, result.getFileUrl());
        verify(chatStorageService).uploadVoiceNote(voiceFile);
    }

    @Test
    void sendMessage_ConsultationNotApproved_ThrowsException() {
        // Arrange
        consultation.setStatus(ConsultationStatus.MENUNGGU_KONFIRMASI_KONSULTAN);
        CreateChatMessageRequest request = new CreateChatMessageRequest();
        request.setMessageType("TEXT");
        request.setMessageContent("Test message");

        when(consultationRepository.findById(consultationId)).thenReturn(Optional.of(consultation));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> chatMessageService.sendMessage(consultationId, homeOwnerId, request));
        assertEquals("Chat is only allowed for approved consultations", exception.getMessage());
    }

    @Test
    void getChatMessages_Success() {
        // Arrange
        ChatMessage message1 = new ChatMessage();
        message1.setId(UUID.randomUUID());
        message1.setSenderId(homeOwnerId);
        message1.setReceiverId(consultantId);
        message1.setMessageType(ChatMessage.MessageType.TEXT);
        message1.setMessageContent("Message 1");

        ChatMessage message2 = new ChatMessage();
        message2.setId(UUID.randomUUID());
        message2.setSenderId(consultantId);
        message2.setReceiverId(homeOwnerId);
        message2.setMessageType(ChatMessage.MessageType.TEXT);
        message2.setMessageContent("Message 2");

        List<ChatMessage> messages = List.of(message1, message2);
        List<ChatMessageDTO> expectedDTOs = List.of(
                ChatMessageDTO.builder().id(message1.getId()).messageContent("Message 1").build(),
                ChatMessageDTO.builder().id(message2.getId()).messageContent("Message 2").build()
        );

        when(consultationRepository.findById(consultationId)).thenReturn(Optional.of(consultation));
        when(chatMessageRepository.findByConsultationIdOrderByCreatedAtAsc(consultationId)).thenReturn(messages);
        when(chatMessageMapper.toDTO(message1)).thenReturn(expectedDTOs.get(0));
        when(chatMessageMapper.toDTO(message2)).thenReturn(expectedDTOs.get(1));

        // Act
        List<ChatMessageDTO> result = chatMessageService.getChatMessages(consultationId, homeOwnerId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Message 1", result.get(0).getMessageContent());
        assertEquals("Message 2", result.get(1).getMessageContent());
    }

    @Test
    void getLatestMessage_Success() {
        // Arrange
        ChatMessage latestMessage = new ChatMessage();
        latestMessage.setId(UUID.randomUUID());
        latestMessage.setSenderId(consultantId);
        latestMessage.setReceiverId(homeOwnerId);
        latestMessage.setMessageType(ChatMessage.MessageType.TEXT);
        latestMessage.setMessageContent("Latest message");

        ChatMessageDTO expectedDTO = ChatMessageDTO.builder()
                .id(latestMessage.getId())
                .messageContent("Latest message")
                .build();

        when(consultationRepository.findById(consultationId)).thenReturn(Optional.of(consultation));
        when(chatMessageRepository.findLatestByConsultationId(consultationId)).thenReturn(latestMessage);
        when(chatMessageMapper.toDTO(latestMessage)).thenReturn(expectedDTO);

        // Act
        ChatMessageDTO result = chatMessageService.getLatestMessage(consultationId, homeOwnerId);

        // Assert
        assertNotNull(result);
        assertEquals("Latest message", result.getMessageContent());
    }
}