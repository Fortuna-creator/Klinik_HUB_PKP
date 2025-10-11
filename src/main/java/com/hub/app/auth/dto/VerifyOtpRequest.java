package com.hub.app.auth.dto;

import lombok.Data;


@Data
public class VerifyOtpRequest {
    private Long userId;
    private String otpCode;

    public Long getUserId() {
        return this.userId;
    }

    public String getOtpCode() {
        return this.otpCode;
    }
}
