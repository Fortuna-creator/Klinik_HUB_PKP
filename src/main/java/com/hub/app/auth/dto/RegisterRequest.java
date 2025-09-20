package com.hub.app.auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegisterRequest {
    private String username;
    private String password;
    private String deviceId;
    private String deviceName;
    private String deviceLocation;
    private String osName;
    private String osVersion;
}
