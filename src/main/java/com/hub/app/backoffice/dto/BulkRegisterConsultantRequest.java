package com.hub.app.backoffice.dto;

import lombok.Data;

import java.util.List;

@Data
public class BulkRegisterConsultantRequest {
    private List<RegisterConsultantRequest> consultants;
}
