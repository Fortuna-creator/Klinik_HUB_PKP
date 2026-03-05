package com.hub.app.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class LoginResponse {
    private Long userId;
    private String fullName;
    private String email;
    private String token;
    private String role;
    private String status;
    private String activityStatus;
    private UUID balaiId;        // Balai staff: which Balai they're assigned to
    private Long expiresIn;
}
