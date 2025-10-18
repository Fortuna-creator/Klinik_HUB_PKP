package com.hub.app.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateResponse {
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String avatarUrl;
    private String bio;
    private String ktpNumber;
    private String ktpImageUrl;
    private Instant updatedAt;
}