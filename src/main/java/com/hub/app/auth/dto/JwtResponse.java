package com.hub.app.auth.dto;

import lombok.Data;

@Data
public class JwtResponse {
    private String token;
    private String role;
    private String email;
}