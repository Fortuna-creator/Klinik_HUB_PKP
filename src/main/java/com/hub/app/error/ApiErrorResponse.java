package com.hub.app.error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
public record ApiErrorResponse(String errorCode, String message) {
}
