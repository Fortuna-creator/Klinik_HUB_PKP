package com.hub.app.error;

import lombok.Builder;

@Builder
public record ApiErrorResponse(String errorCode, String message) {
}
