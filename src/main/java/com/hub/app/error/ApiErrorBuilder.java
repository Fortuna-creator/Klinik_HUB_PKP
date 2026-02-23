package com.hub.app.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ApiErrorBuilder {
    public ResponseEntity<ApiErrorResponse> error(HttpStatus status, String errorCode, String message) {
        return ResponseEntity.status(status)
                .body(ApiErrorResponse.builder()
                        .errorCode(errorCode)
                        .message(message)
                        .build());
    }
}
