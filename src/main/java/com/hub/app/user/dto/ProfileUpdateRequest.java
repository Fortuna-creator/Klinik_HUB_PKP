package com.hub.app.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileUpdateRequest {
    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    @Size(max = 15, message = "Phone number must not exceed 15 characters")
    private String phone;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @Size(max = 255, message = "Avatar URL must not exceed 255 characters")
    private String avatarUrl;

    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;

    @NotBlank(message = "KTP number is required")
    @Pattern(regexp = "^[0-9]{16}$", message = "KTP number must be 16 digits")
    private String ktpNumber;

    @NotBlank(message = "KTP image URL is required")
    @Size(max = 255, message = "KTP image URL must not exceed 255 characters")
    private String ktpImageUrl;
}