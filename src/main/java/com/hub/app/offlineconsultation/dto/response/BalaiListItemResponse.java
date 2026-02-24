package com.hub.app.offlineconsultation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response item for GET /api/balai/list.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BalaiListItemResponse {

    private String id;
    private String name;
    private String location;
    private String phone;
    private String email;
    private String address;
    private WorkingHoursDto workingHours;
}
