package com.hub.app.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private Long userId;
    private String fullName;
    private String email;
    private String token;
    private String role;
    private String status;
    private Integer expiresIn;
}
