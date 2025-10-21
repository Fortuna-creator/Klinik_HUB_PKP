package com.hub.app.consultation.dto;

import com.hub.app.consultation.model.ConsultationChannel;
import com.hub.app.consultation.model.ConsultationType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateConsultationRequest {
    @NotNull
    private Long consultantId;

    @NotNull
    private ConsultationType consultationType;

    @NotNull
    private ConsultationChannel channel;
}