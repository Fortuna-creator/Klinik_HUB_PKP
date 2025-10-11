package com.hub.app.auth.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;

    // Explicit getters for IDEs that don't run Lombok processors
    public String getEmail() {
        return this.email;
    }

    public String getPassword() {
        return this.password;
    }
}