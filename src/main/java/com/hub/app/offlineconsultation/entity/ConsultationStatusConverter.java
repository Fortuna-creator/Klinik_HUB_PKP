package com.hub.app.offlineconsultation.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA converter that stores ConsultationStatus as its lowercase Indonesian
 * string value (e.g. "menunggu_konfirmasi") in the database column.
 */
@Converter(autoApply = false)
public class ConsultationStatusConverter implements AttributeConverter<ConsultationStatus, String> {

    @Override
    public String convertToDatabaseColumn(ConsultationStatus status) {
        return status == null ? null : status.getValue();
    }

    @Override
    public ConsultationStatus convertToEntityAttribute(String value) {
        return value == null ? null : ConsultationStatus.fromValue(value);
    }
}
