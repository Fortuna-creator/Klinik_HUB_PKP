package com.hub.app.offlineconsultation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Nested user summary embedded inside consultation responses.
 * Maps to the contract's {@code "user": { id, name, phone, email, avatar }}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryDto {

    private String id;
    private String name;
    private String phone;
    private String email;
    private String avatar;    // URL
}
