package com.hub.app.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProfileUpdateRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    private String phone;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "KTP Number is required")
    @Pattern(regexp = "^[0-9]{16}$", message = "KTP Number must be 16 digits")
    private String ktpNumber;

    private MultipartFile ktpFile;
    private MultipartFile selfieFile;
}