package com.hub.app.consultation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hub.app.consultation.dto.ChatMessagePayload;
import com.hub.app.consultation.dto.ChatMessageResponse;
import com.hub.app.consultation.event.ChatMessageCreatedEvent;
import com.hub.app.consultation.model.ChatMessage;
import com.hub.app.consultation.model.Consultation;
import com.hub.app.consultation.model.ConsultationStatus;
import com.hub.app.consultation.repository.ChatMessageRepository;
import com.hub.app.consultation.repository.ConsultationRepository;
import com.hub.app.user.model.Consultant;
import com.hub.app.user.model.HomeOwner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatWebSocketController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChatWebSocketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatMessageRepository chatMessageRepository;

    @MockitoBean
    private ConsultationRepository consultationRepository;

    @MockitoBean
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ObjectMapper objectMapper;

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
    void contextLoads() {
        // Test that the WebSocket controller context loads properly
        // This is a basic smoke test for the WebSocket configuration
    }

    // Note: Full WebSocket integration testing would require WebSocket test utilities
    // like WebSocketStompClient for more comprehensive testing.
    // For now, we rely on the existing REST API tests and manual testing.
}