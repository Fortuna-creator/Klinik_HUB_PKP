package com.hub.app.consultation.event;

import com.hub.app.consultation.model.ChatMessage;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ChatMessageCreatedEvent extends ApplicationEvent {

    private final ChatMessage chatMessage;

    public ChatMessageCreatedEvent(Object source, ChatMessage chatMessage) {
        super(source);
        this.chatMessage = chatMessage;
    }
}