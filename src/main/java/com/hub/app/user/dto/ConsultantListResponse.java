package com.hub.app.user.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ConsultantListResponse {
    private List<ConsultantResponse> consultants;
    private Pagination pagination;

    @Data
    @Builder
    public static class Pagination {
        private Integer total;
        private Integer page;
        private Integer size;
    }
}