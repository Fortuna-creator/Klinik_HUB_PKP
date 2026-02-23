package com.hub.app.consultation.repository;

import com.hub.app.consultation.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    List<ChatMessage> findByConsultationIdOrderByCreatedAtAsc(UUID consultationId);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.consultation.id = :consultationId ORDER BY cm.createdAt DESC LIMIT 1")
    ChatMessage findLatestByConsultationId(@Param("consultationId") UUID consultationId);
}