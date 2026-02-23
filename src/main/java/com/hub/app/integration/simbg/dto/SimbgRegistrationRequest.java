package com.hub.app.integration.simbg.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO for SIMBG application registration request.
 */
public class SimbgRegistrationRequest {

    @JsonProperty("applicantName")
    private String applicantName;

    @JsonProperty("applicantIdNumber")
    private String applicantIdNumber;

    @JsonProperty("propertyAddress")
    private String propertyAddress;

    @JsonProperty("prototypeType")
    private String prototypeType; // PROTOTYPE / NON_PROTOTYPE

    @JsonProperty("externalReference")
    private String externalReference;

    @JsonProperty("documents")
    private List<DocumentDto> documents;

    // Getters and Setters
    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public String getApplicantIdNumber() {
        return applicantIdNumber;
    }

    public void setApplicantIdNumber(String applicantIdNumber) {
        this.applicantIdNumber = applicantIdNumber;
    }

    public String getPropertyAddress() {
        return propertyAddress;
    }

    public void setPropertyAddress(String propertyAddress) {
        this.propertyAddress = propertyAddress;
    }

    public String getPrototypeType() {
        return prototypeType;
    }

    public void setPrototypeType(String prototypeType) {
        this.prototypeType = prototypeType;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }

    public List<DocumentDto> getDocuments() {
        return documents;
    }

    public void setDocuments(List<DocumentDto> documents) {
        this.documents = documents;
    }
}