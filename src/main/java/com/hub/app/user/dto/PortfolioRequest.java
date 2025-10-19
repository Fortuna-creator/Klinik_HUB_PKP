package com.hub.app.user.dto;

import lombok.Data;

import java.util.List;

@Data
public class PortfolioRequest {
    private String bio;
    private String specialization;
    private String experience;
    private String education;
    private String certifications;
    private String linkedinUrl;
    private String websiteUrl;
    // Projects are now optional - consultants can add them separately
    private List<PortfolioProjectRequest> projects;
}