package com.hub.app.auth.dto;

import lombok.Data;


@Data
public class VerifyOtpRequest {
    private Long userId;
    private String otpCode;
}
