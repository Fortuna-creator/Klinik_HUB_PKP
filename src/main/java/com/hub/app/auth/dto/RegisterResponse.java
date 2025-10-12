package com.hub.app.auth.dto;

import com.hub.app.user.model.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterResponse {
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private String username;
    private Role role;
    private String status;
    private String ktpUrl;
    private String selfieUrl;
}
