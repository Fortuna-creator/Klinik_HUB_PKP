--liquibase formatted sql

--changeset hub:13
-- Chat Messages Schema

-- Create chat_messages table
CREATE TABLE IF NOT EXISTS chat_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    consultation_id UUID NOT NULL REFERENCES consultations(id),
    sender_id BIGINT NOT NULL REFERENCES users(id),
    receiver_id BIGINT NOT NULL REFERENCES users(id),
    message_type VARCHAR(50) NOT NULL,
    message_content TEXT,
    file_url VARCHAR(500),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_chat_messages_consultation ON chat_messages(consultation_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_sender ON chat_messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_receiver ON chat_messages(receiver_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_created_at ON chat_messages(created_at);