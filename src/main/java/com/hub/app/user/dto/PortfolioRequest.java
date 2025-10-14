package com.hub.app.user.dto;

import lombok.Data;

@Data
public class PortfolioRequest {
    private String bio;
    private String specialization;
    private String experience;
    private String education;
    private String certifications;
    private String projectSamples;
    private String linkedinUrl;
    private String websiteUrl;
}