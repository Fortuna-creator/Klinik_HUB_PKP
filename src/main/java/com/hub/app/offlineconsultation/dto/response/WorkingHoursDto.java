package com.hub.app.offlineconsultation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Nested working-hours object inside {@link BalaiListItemResponse}.
 * Maps to the contract's {@code "workingHours": { start, end }}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkingHoursDto {

    private String start;   // e.g. "07.00"
    private String end;     // e.g. "16.00"
}
