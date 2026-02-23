package com.hub.app.backoffice.dto;

import lombok.Data;

@Data
public class RegisterConsultantRequest {
    private String name;
    private String email;
    private String phone;
    private String university;   // khusus mahasiswa
    private boolean professional; // true = konsultan profesional, false = mahasiswa
}
