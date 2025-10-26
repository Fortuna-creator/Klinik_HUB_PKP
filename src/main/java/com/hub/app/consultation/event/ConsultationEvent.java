package com.hub.app.consultation.event;

import com.hub.app.consultation.model.Consultation;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class ConsultationEvent extends ApplicationEvent {

    private final Consultation consultation;
    private final String eventType;
    private final String step;
    private final String state;
    private final String actor;
    private final List<String> files;
    private final List<String> attachments;
    private final BigDecimal surveyCost;
    private final String surveyLocation;

    public ConsultationEvent(Object source, Consultation consultation, String eventType,
                           String step, String state, String actor) {
        super(source);
        this.consultation = consultation;
        this.eventType = eventType;
        this.step = step;
        this.state = state;
        this.actor = actor;
        this.files = null;
        this.attachments = null;
        this.surveyCost = null;
        this.surveyLocation = null;
    }

    public ConsultationEvent(Object source, Consultation consultation, String eventType,
                           String step, String state, String actor, List<String> files,
                           List<String> attachments, BigDecimal surveyCost, String surveyLocation) {
        super(source);
        this.consultation = consultation;
        this.eventType = eventType;
        this.step = step;
        this.state = state;
        this.actor = actor;
        this.files = files;
        this.attachments = attachments;
        this.surveyCost = surveyCost;
        this.surveyLocation = surveyLocation;
    }
}